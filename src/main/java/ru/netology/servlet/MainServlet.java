package ru.netology.servlet;

import ru.netology.controller.PostController;
import ru.netology.repository.PostRepository;
import ru.netology.service.PostService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

public class MainServlet extends HttpServlet {
    private PostController controller;

    // Константы для путей и статусов
    private static final String API_POSTS_PATH = "/api/posts";
    private static final Pattern POST_ID_PATTERN = Pattern.compile(API_POSTS_PATH + "/(\\d+)");
    private static final int STATUS_NOT_FOUND = HttpServletResponse.SC_NOT_FOUND;
    private static final int STATUS_INTERNAL_ERROR = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

    @Override
    public void init() {
        final var repository = new PostRepository();
        final var service = new PostService(repository);
        controller = new PostController(service);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final String path = req.getRequestURI();
            final String method = req.getMethod();

            // Роутинг по методам и путям
            if ("GET".equals(method)) {
                handleGet(path, resp);
                return;
            }
            if ("POST".equals(method) && API_POSTS_PATH.equals(path)) {
                controller.save(req.getReader(), resp);
                return;
            }
            if ("DELETE".equals(method) && POST_ID_PATTERN.matcher(path).matches()) {
                final long id = extractIdFromPath(path);
                controller.removeById(id, resp);
                return;
            }

            resp.setStatus(STATUS_NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(STATUS_INTERNAL_ERROR);
        }
    }

    // Приватный метод для обработки GET
    private void handleGet(String path, HttpServletResponse resp) throws Exception {
        if (API_POSTS_PATH.equals(path)) {
            controller.all(resp);
        } else if (POST_ID_PATTERN.matcher(path).matches()) {
            final long id = extractIdFromPath(path);
            controller.getById(id, resp);
        } else {
            resp.setStatus(STATUS_NOT_FOUND);
        }
    }

    // Извлечение ID из пути (убирает дублирование)
    private long extractIdFromPath(String path) {
        return Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
    }
}
