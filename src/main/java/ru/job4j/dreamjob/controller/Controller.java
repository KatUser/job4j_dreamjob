package ru.job4j.dreamjob.controller;

import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.stereotype.Controller
public class Controller {
    @GetMapping("/page")
    public String getPage() {
        return "page";
    }

    @GetMapping("/table")
    public String getTable() {
        return "table";
    }
}
