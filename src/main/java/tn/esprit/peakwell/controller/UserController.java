package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.entities.User;
import tn.esprit.peakwell.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/user")
public class UserController {

  @Autowired
  IUserService userService;

  @GetMapping("/{id}")
  public User getUserById(@PathVariable Long id) {
    return userService.getUserById(id);
  }
}
