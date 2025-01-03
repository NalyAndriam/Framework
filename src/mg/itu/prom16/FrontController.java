package mg.itu.prom16;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import manager.MainProcess;
import exception.DuplicateUrlException;
import exception.IllegalReturnTypeException;
import exception.InvalidControllerPackageException;
import exception.UrlNotFoundException;
import handler.ExceptionHandler;
import util.Mapping;

@MultipartConfig
public class FrontController extends HttpServlet {
    private Map<String, Mapping> URLMappings;
    private Exception exception = null;

    // Class methods
    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            MainProcess.handleRequest(this, request, response);
        } catch (UrlNotFoundException | IllegalReturnTypeException e) {
            ExceptionHandler.handleException(e, response);
        } catch (Exception e) {
            ExceptionHandler.handleException(e, response);
        }
    }

    // Override methods
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequest(req, resp);
        } catch (ServletException e) {
            ExceptionHandler.handleException(
                    new ServletException("A servlet error has occured while executing doGet method", e.getCause()),
                    resp);
        } catch (IOException e) {
            ExceptionHandler.handleException(
                    new IOException("An IO error has occured while executing doGet method", e.getCause()), resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequest(req, resp);
        } catch (ServletException e) {
            ExceptionHandler.handleException(
                    new ServletException("A servlet error has occured while executing doPost method", e.getCause()),
                    resp);
        } catch (IOException e) {
            ExceptionHandler.handleException(
                    new IOException("An IO error has occured while executing doPost method", e.getCause()), resp);
        }
    }

    @Override
    public void init() throws ServletException {
        try {
            MainProcess.init(this);
        } catch (InvalidControllerPackageException | DuplicateUrlException e) {
            setException(e);
        } catch (Exception e) {
            setException(new Exception("An error has occured during initialization + " + e.getMessage(), e.getCause()));
        }
    }

    // Getters and setters
    public Map<String, Mapping> getURLMapping() {
        return URLMappings;
    }

    public void setURLMapping(Map<String, Mapping> urlMapping) {
        this.URLMappings = urlMapping;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}