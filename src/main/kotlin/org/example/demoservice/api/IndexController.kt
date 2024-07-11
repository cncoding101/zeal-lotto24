package org.example.demoservice.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class IndexController {

    @RequestMapping("/index.html", "/index.htm", "/")
    fun redirectToSwaggerDoc(): String {
        return "redirect:swagger-ui.html"
    }
}
