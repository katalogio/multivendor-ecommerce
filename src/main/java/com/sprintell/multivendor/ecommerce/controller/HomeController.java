package com.sprintell.multivendor.ecommerce.controller;

import com.sprintell.multivendor.ecommerce.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping
    public ApiResponse HomeControllerHandler (){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Welcome to ecommerce multi vendor system");
        return apiResponse;
    }
}
