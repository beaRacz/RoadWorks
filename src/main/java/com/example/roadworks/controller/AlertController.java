package com.example.roadworks.controller;

import com.example.roadworks.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("alerts", alertService.findAll());
        model.addAttribute("unreadAlertCount", alertService.countUnread());
        return "alerts/list";
    }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        alertService.markAsRead(id);
        redirectAttrs.addFlashAttribute("successMessage", "Alert marked as read.");
        return "redirect:/alerts";
    }

    @PostMapping("/read-all")
    public String markAllRead(RedirectAttributes redirectAttrs) {
        alertService.markAllAsRead();
        redirectAttrs.addFlashAttribute("successMessage", "All alerts marked as read.");
        return "redirect:/alerts";
    }
}
