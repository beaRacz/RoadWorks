package com.example.roadworks.controller;

import com.example.roadworks.model.User;
import com.example.roadworks.model.enums.UserRole;
import com.example.roadworks.service.AlertService;
import com.example.roadworks.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AlertService alertService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("unreadAlertCount", alertService.countUnread());
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("isNew", true);
        model.addAttribute("unreadAlertCount", alertService.countUnread());
        return "admin/users/form";
    }

    @PostMapping("/new")
    public String create(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam String fullName,
                         @RequestParam String email,
                         @RequestParam UserRole role,
                         RedirectAttributes redirectAttrs) {
        if (userService.existsByUsername(username)) {
            redirectAttrs.addFlashAttribute("errorMessage", "Username '" + username + "' already exists.");
            return "redirect:/admin/users/new";
        }
        userService.createUser(username, password, fullName, email, role);
        redirectAttrs.addFlashAttribute("successMessage", "User '" + username + "' created.");
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("isNew", false);
        model.addAttribute("unreadAlertCount", alertService.countUnread());
        return "admin/users/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam String fullName,
                         @RequestParam String email,
                         @RequestParam UserRole role,
                         @RequestParam(defaultValue = "false") boolean active,
                         @RequestParam(required = false) String newPassword,
                         RedirectAttributes redirectAttrs) {
        userService.updateUser(id, fullName, email, role, active);
        if (newPassword != null && !newPassword.isBlank()) {
            userService.changePassword(id, newPassword);
        }
        redirectAttrs.addFlashAttribute("successMessage", "User updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        userService.deleteUser(id);
        redirectAttrs.addFlashAttribute("successMessage", "User deleted.");
        return "redirect:/admin/users";
    }
}
