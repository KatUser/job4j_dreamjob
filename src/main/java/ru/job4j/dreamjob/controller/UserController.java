package ru.job4j.dreamjob.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import ru.job4j.dreamjob.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.job4j.dreamjob.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String getRegistrationPage() {
        return "users/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                           Model model) {
        var savedUser = userService.save(user);
        if (savedUser.isEmpty()) {
            model.addAttribute("message", "Пользователь с таким email уже существует");
            return "errors/404";
        }
        return "redirect:/vacancies";
    }

    @GetMapping("login")
    public String getLoginPage() {
        return "users/login";
    }

    @PostMapping("login")
    public String loginUser(@ModelAttribute User user, /* @ModelAttribute позволяет автоматически связывать
    значения полей объектов модели с элементами формы */
                            Model model,
                            HttpServletRequest request) {
        var userOptional = userService.findByEmailAndPassword(
                user.getEmail(), user.getPassword()
        );
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "Почта или пароль введены неверно");
            return "users/login";
        }
        var httpSession = request.getSession(); /* getSession возвращает объект HttpSession,
        в нём можно хранить инфу о текущем пользователе; в HttpSession используется ConcurrentHashMap,
        для работы с ConcurrentHashMap нельзя использовать операции check-then-act.
        То есть HttpSession можно использовать либо для записи, либо для чтения,
        но нельзя делать это одновременно. */
        httpSession.setAttribute("user", userOptional.get());
        return "redirect:/vacancies";
    }

    @GetMapping("/logout")
    public String logout(HttpSession httpSession) {
        httpSession.invalidate();
        return "redirect:/users/login";
    }
}