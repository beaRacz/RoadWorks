package com.example.roadworks.controller;

import com.example.roadworks.service.AlertService;
import com.example.roadworks.service.RoadWorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final RoadWorkService roadWorkService;
    private final AlertService alertService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("stats", roadWorkService.getDashboardStats());
        model.addAttribute("recentAlerts", alertService.findAllUnread());
        model.addAttribute("activeRoadWorks", roadWorkService.findActive());
        model.addAttribute("expiring7Days", roadWorkService.findExpiringInDays(7));
        model.addAttribute("lateSignWorks", roadWorkService.findWithLateSignRemoval());
        return "dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
