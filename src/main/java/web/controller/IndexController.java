package web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import web.model.User;
import web.service.AppService;

@Controller
@RequestMapping("/")
public class IndexController {
    boolean check = true;

    private final AppService appService;

    public IndexController(AppService userService) {
        this.appService = userService;
    }

    @GetMapping()
    public String redirect() {


        if (check == true) {

            System.out.println("Hello 123");
            appService.query("INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN'), (2, 'ROLE_USER')");
            appService.query("INSERT INTO users (id, email, enabled, name, last_name, password) VALUES (1, 'admin', true, 'Петр', 'Иванов', '$2y$10$kbBc2/YyhalAHuK.SRiFPeu/iENCtVjUS9sK4A3/4b5EXdgqcj0cy')");
            appService.query("INSERT INTO users_roles VALUES (1, 1)");
            check = false;
        }
        return "redirect:/login";
    }
}