package br.com.deveberte.urlshortening.api.resource;

import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/private")
public class HiResource {
    @GetMapping("/hi")
    public String hi(@RequestParam(name = "name") String name){
        return "Hi, "+ name+"!";
    }
}
