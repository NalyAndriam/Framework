package manager;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;

import annotation.RequiredRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import manager.data.ModelValidationExceptionHandler;
import manager.handler.RedirectionHandler;
import mg.itu.prom16.FrontController;
import utils.ModelView;
import util.VerbMethod;
import exception.AnnotationNotPresentException;
import exception.DuplicateUrlException;
import exception.IllegalReturnTypeException;
import exception.InvalidControllerPackageException;
import exception.InvalidRequestException;
import exception.ModelValidationException;
import exception.UnauthorizedAccessException;
import exception.UrlNotFoundException;
import handler.ExceptionHandler;
import util.Mapping;
import utils.PackageScanner;
import utils.ReflectUtils;
import utils.RequestUtil;
import utils.validation.Validator;
import manager.data.InitParameter;

public class MainProcess {
    static FrontController frontController;
    private List<Exception> exceptions;
    private static ModelValidationExceptionHandler handler = new ModelValidationExceptionHandler();
    private static String defaultRoleAttribute;

    private static void checkUserRole(HttpServletRequest request, VerbMethod verbMethod)
            throws UnauthorizedAccessException {

        RequiredRole requiredRole = verbMethod.getMethod().getAnnotation(RequiredRole.class);
        if (requiredRole != null) {
            HttpSession session = request.getSession(false);

            if (session == null) {
                throw new UnauthorizedAccessException("No active session found");
            }

            Object role = session.getAttribute(defaultRoleAttribute);

            if (role == null) {
                throw new UnauthorizedAccessException("No role defined in session");
            }

            String roleStr = role.toString();
            String[] allowedRoles = requiredRole.values();
            boolean hasRequiredRole = false;
            for (String allowed : allowedRoles) {
                if (allowed.equalsIgnoreCase(roleStr)) {
                    hasRequiredRole = true;
                    break;
                }
            }
            if (!hasRequiredRole) {
                throw new UnauthorizedAccessException(
                        "Required role not found. Required: " + Arrays.toString(allowedRoles));
            }
        }
    }

    private static boolean hasRequiredRole(String userRole, String[] requiredRoles) {
        if (userRole == null || requiredRoles == null || requiredRoles.length == 0) {
            return false;
        }

        for (String required : requiredRoles) {
            if (required.equalsIgnoreCase(userRole)) {
                return true;
            }
        }
        return false;
    }

    private static String getUserRoleFromSession(HttpServletRequest request)
        throws UnauthorizedAccessException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new UnauthorizedAccessException("No active session found");
        }

       
        Object role = session.getAttribute(defaultRoleAttribute);
        if (role == null) {
            throw new UnauthorizedAccessException("No role defined in session");
        }

        return role.toString();
    }


    private static String handleRest(Object methodObject, HttpServletResponse response) {
        Gson gson = new Gson();
        String json = null;
        if (methodObject instanceof ModelView modelView) {
            json = gson.toJson((modelView).getData());
        } else {
            json = gson.toJson(methodObject);
        }   
        response.setContentType("application/json");
        return json;
    }

    private static void prepareRequest(FrontController controller, HttpServletRequest request) {
        if (handler == null)
            handler = new ModelValidationExceptionHandler();
        if (request.getAttribute(controller.getInitParameter().getErrorParamName()) == null) {
            request.setAttribute(controller.getInitParameter().getErrorParamName(), handler);
        }
    }


    public static void handleRequest(FrontController controller, HttpServletRequest request,
            HttpServletResponse response) throws IOException, UrlNotFoundException, 
            NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, InstantiationException, ServletException, IllegalReturnTypeException, 
            NoSuchFieldException, AnnotationNotPresentException, InvalidRequestException, 
            ModelValidationException, ClassNotFoundException, UnauthorizedAccessException {
        PrintWriter out = response.getWriter();
        String verb = request.getMethod();

        if (controller.getException() != null) {
            ExceptionHandler.handleException(controller.getException(), response);
            return;
        }

        String url = request.getRequestURI().substring(request.getContextPath().length());
        Mapping mapping = frontController.getURLMapping().get(url);
        
        if (mapping == null) {
            throw new UrlNotFoundException("Oops, url not found!(" + url + ")");
        }
        
        VerbMethod verbMethod = mapping.getSpecificVerbMethod(verb);

        checkUserRole(request, verbMethod);
        
        handler = Validator.validateMethod(verbMethod.getMethod(), request);

        Object result;

        if (handler.containsException()) {
            ModelView modelView = new ModelView();
            modelView.setRedirect(false);
            modelView.setUrl(request.getParameter(controller.getInitParameter().getErrorRedirectionParamName()));

            request = RequestUtil.generateHttpServletRequest(request, "GET");

            if (verbMethod.isRestAPI()) {
                modelView.addObject(controller.getInitParameter().getErrorParamName(), handler);
            }

            result = modelView;
        } else {
            result = ReflectUtils.executeRequestMethod(mapping, request, verb);
        }

        prepareRequest(controller, request);

        if (verbMethod.isRestAPI()) {
            result = handleRest(result, response);
        }   

        if (result instanceof String) {
            out.println(result.toString());
        } else if (result instanceof ModelView modelView) {
            RedirectionHandler.redirect(request, response, modelView);
        } else {
            throw new IllegalReturnTypeException("Invalid return type");
        }
    }

    public static void init(FrontController controller)
            throws ClassNotFoundException, IOException, DuplicateUrlException, InvalidControllerPackageException {
        frontController = controller;

        String packageName = controller.getInitParameter("package_name");
        String errorParamName = controller.getInitParameter("error_param_name");
        String errorRedirectionParamName = controller.getInitParameter("error_redirection_param_name");
        String roleAttributeName = controller.getInitParameter("role_attribute_name");

        HashMap<String, Mapping> urlMappings;
        urlMappings = (HashMap<String, Mapping>) PackageScanner.scanPackage(packageName);

        InitParameter initParameter = new InitParameter(errorParamName, packageName, errorRedirectionParamName,
                roleAttributeName);

        defaultRoleAttribute = initParameter.getRoleAttributeName();

        controller.setURLMapping(urlMappings);
        controller.setInitParameter(initParameter);
    }

    // Getters and setters
    public List<Exception> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<Exception> exceptions) {
        this.exceptions = exceptions;
    }
}
