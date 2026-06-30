package com.example.roadworks.controller;

import com.example.roadworks.model.RoadWork;
import com.example.roadworks.model.User;
import com.example.roadworks.model.enums.WorkStatus;
import com.example.roadworks.service.AlertService;
import com.example.roadworks.service.RoadWorkService;
import com.example.roadworks.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;

@Controller
@RequestMapping("/roadworks")
@RequiredArgsConstructor
public class RoadWorkController {

    private final RoadWorkService roadWorkService;
    private final AlertService alertService;
    private final UserService userService;

    @GetMapping
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) String county,
                       @RequestParam(required = false) String roadCode,
                       Model model) {
        var roadWorks = roadWorkService.findAll();

        // Simple client-side filtering for H2
        if (status != null && !status.isEmpty()) {
            WorkStatus ws = WorkStatus.valueOf(status);
            roadWorks = roadWorks.stream()
                    .filter(rw -> rw.getStatus() == ws)
                    .toList();
        }
        if (county != null && !county.isEmpty()) {
            roadWorks = roadWorks.stream()
                    .filter(rw -> county.equalsIgnoreCase(rw.getCounty()))
                    .toList();
        }
        if (roadCode != null && !roadCode.isEmpty()) {
            roadWorks = roadWorks.stream()
                    .filter(rw -> roadCode.equalsIgnoreCase(rw.getRoadCode()))
                    .toList();
        }

        model.addAttribute("roadWorks", roadWorks);
        model.addAttribute("statuses", WorkStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCounty", county);
        model.addAttribute("selectedRoadCode", roadCode);
        model.addAttribute("unreadAlertCount", alertService.countUnread());
        return "roadworks/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("roadWork", new RoadWork());
        model.addAttribute("statuses", WorkStatus.values());
        model.addAttribute("isNew", true);
        model.addAttribute("unreadAlertCount", alertService.countUnread());
        return "roadworks/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("roadWork") RoadWork roadWork,
                         BindingResult result, Authentication auth,
                         RedirectAttributes redirectAttrs, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", WorkStatus.values());
            model.addAttribute("isNew", true);
            return "roadworks/form";
        }
        User currentUser = getCurrentUser(auth);
        roadWorkService.save(roadWork, currentUser);
        redirectAttrs.addFlashAttribute("successMessage", "Road work created successfully.");
        return "redirect:/roadworks";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        RoadWork rw = roadWorkService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Road work not found: " + id));
        model.addAttribute("roadWork", rw);
        model.addAttribute("alerts", alertService.findByRoadWork(rw));
        model.addAttribute("unreadAlertCount", alertService.countUnread());
        return "roadworks/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        RoadWork rw = roadWorkService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Road work not found: " + id));
        model.addAttribute("roadWork", rw);
        model.addAttribute("statuses", WorkStatus.values());
        model.addAttribute("isNew", false);
        model.addAttribute("unreadAlertCount", alertService.countUnread());
        return "roadworks/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("roadWork") RoadWork roadWork,
                         BindingResult result, Authentication auth,
                         RedirectAttributes redirectAttrs, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", WorkStatus.values());
            model.addAttribute("isNew", false);
            return "roadworks/form";
        }
        roadWork.setId(id);
        User currentUser = getCurrentUser(auth);
        roadWorkService.save(roadWork, currentUser);
        redirectAttrs.addFlashAttribute("successMessage", "Road work updated.");
        return "redirect:/roadworks/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        roadWorkService.delete(id);
        redirectAttrs.addFlashAttribute("successMessage", "Road work deleted.");
        return "redirect:/roadworks";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttrs) {
        roadWorkService.complete(id, getCurrentUser(auth));
        redirectAttrs.addFlashAttribute("successMessage", "Road work marked as completed.");
        return "redirect:/roadworks/" + id;
    }

    @PostMapping("/{id}/extend")
    public String extend(@PathVariable Long id,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEndDate,
                         @RequestParam(defaultValue = "") String reason,
                         Authentication auth,
                         RedirectAttributes redirectAttrs) {
        roadWorkService.extend(id, newEndDate, reason, getCurrentUser(auth));
        redirectAttrs.addFlashAttribute("successMessage", "Period extended until " + newEndDate + ".");
        return "redirect:/roadworks/" + id;
    }

    @PostMapping("/{id}/signs-placed")
    public String signsPlaced(@PathVariable Long id,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedRemoval,
                              Authentication auth,
                              RedirectAttributes redirectAttrs) {
        roadWorkService.markSignsPlaced(id, expectedRemoval, getCurrentUser(auth));
        redirectAttrs.addFlashAttribute("successMessage", "Temporary signs marked as placed.");
        return "redirect:/roadworks/" + id;
    }

    @PostMapping("/{id}/signs-removed")
    public String signsRemoved(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttrs) {
        roadWorkService.markSignsRemoved(id, getCurrentUser(auth));
        redirectAttrs.addFlashAttribute("successMessage", "Temporary signs marked as removed.");
        return "redirect:/roadworks/" + id;
    }

    // ---- Helper ----

    private User getCurrentUser(Authentication auth) {
        return userService.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
    }
}
