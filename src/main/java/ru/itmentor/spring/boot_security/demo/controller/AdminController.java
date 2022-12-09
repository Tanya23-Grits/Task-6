package ru.itmentor.spring.boot_security.demo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import ru.itmentor.spring.boot_security.demo.model.Role;
import ru.itmentor.spring.boot_security.demo.model.User;
import ru.itmentor.spring.boot_security.demo.repository.UserRepository;

import javax.validation.Valid;
import java.util.*;


@Controller
public class AdminController {

    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @GetMapping(value = "/admin")
    public String adminPage(ModelMap model) {
        List<User> userList = userRepository.findAll();
        model.addAttribute("users", userList);

        User currentUser = getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", new User());
        return "admin_page";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByName(authentication.getName());
    }


    @GetMapping("/admin/addUser")
    public String addUser(Model model) {
        User user = new User();
        model.addAttribute("user", user);

        model.addAttribute("currentUser", getCurrentUser());
        return "add_user";
    }

    @RequestMapping(value = "/admin/updateUser", method = RequestMethod.POST)
    public String updateUser(User updatedUser) {
        Optional<User> userOptional = userRepository.findById(updatedUser.getId());
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            if (!updatedUser.getPassword().isBlank()) {
                existingUser.setPassword(updatedUser.getPassword());
            }
            if (updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty()) {
                existingUser.setRoles(updatedUser.getRoles());
            }
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setAge(updatedUser.getAge());
            userRepository.save(existingUser);

        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/saveUser")
    public String saveNewUser(@ModelAttribute("user") @Valid User user, @RequestParam(value = "userRoles", required = false) List<String> userRoles) {
        User presentUser = userRepository.findByName(user.getName());
        if (presentUser == null) {
            if (userRoles != null && !userRoles.isEmpty()) {
                Set<Role> roles = new HashSet<>();
                userRoles.forEach(r -> roles.add(new Role(r)));
                user.setRoles(roles);
            } else {
                user.setRoles(Collections.singleton(new Role("ROLE_USER")));
            }
            userRepository.save(user);
        } else {
            return "redirect:/admin/addUser";
        }
        return "redirect:/admin";
    }

    @RequestMapping(value = "/admin/deleteUser", method = RequestMethod.DELETE)
    public String deleteUser(User userDto) {
        User user = userRepository.findByName(userDto.getName());
        userRepository.delete(user);
        return "redirect:/admin";
    }
}

