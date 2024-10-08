package com.example.word_guess_game;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String homepage() {
        return "home";
    }

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    @PostMapping("/signup-v")
    public String signup(@ModelAttribute("user") User user, @RequestParam String name, @RequestParam String email,
                         @RequestParam String password, @RequestParam String verifyPassword,
                         Model model) {
        if (!password.equals(verifyPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match");
            return "signup";
        }

        // Step 1: Check if any field is empty
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            model.addAttribute("errorMessage", "All fields are required");
            return "signup"; // Return the signup page with an error message
        }

        // Step 2: Validate email format (simple regex)
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            model.addAttribute("errorMessage", "Invalid email format");
            return "signup";
        }

        // Step 3: Validate password length
        if (password.length() < 6) {
            model.addAttribute("errorMessage", "Password must be at least 6 characters long");
            return "signup";
        }

        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            model.addAttribute("errorMessage", "Email is already registered");
            return "signup";
        }

        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password); // Ensure password is hashed before saving in production
        newUser.setType("user"); // Set type to "user"
        newUser.setScore(0L); // Set initial score to 0
        userRepository.save(newUser);

        model.addAttribute("successMessage", "User created successfully");
        return "signup";
    }

    @GetMapping("/login")
    public String index() {
        return "login";
    }

    @PostMapping("/login-v")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) { // Password should be hashed and compared
            session.setAttribute("user", user);

            if ("admin".equals(user.getType())) {
                return "redirect:/admin/words";
            } else if ("user".equals(user.getType())) {
                return "redirect:/dashboard";
            }
            return "login";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "redirect:/";
    }

    @GetMapping("/Score/{userId}/{newScore}")
    public String updateScore(HttpSession session, Model model, @PathVariable Long userId, @PathVariable int newScore) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            user.setScore((long) newScore);
            userService.updateScore(userId, newScore);
            session.setAttribute("win_message", "Congratulations You win");
            model.addAttribute("user", user);
            session.setAttribute("user", user);
            return "redirect:/dashboard";
        } else {
            return "redirect:/";
        }
    }
}
