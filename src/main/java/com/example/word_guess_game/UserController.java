package com.example.word_guess_game;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private WordRepository wordRepository;

    @GetMapping("/dashboard")
    public String showForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        String loosemessage = (String) session.getAttribute("loose_message");
        String winmessage = (String) session.getAttribute("win_message");
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("levels", new String[]{"Easy", "Medium", "Hard"});
            model.addAttribute("selectedLevel", "");
            model.addAttribute("message", loosemessage);
            model.addAttribute("message1", winmessage);
            return "word-form";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/word")
    public String getWord(@ModelAttribute("selectedLevel") String selectedLevel,
                          Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        session.setAttribute("win_message", null);
        if (user != null) {
            Word word = wordRepository.findRandomWordByLevel(selectedLevel);
            model.addAttribute("word", word);
            session.setAttribute("word", word);
            session.setAttribute("selectedLevel", selectedLevel);
            model.addAttribute("user", user);
            return "redirect:/showWord";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/showWord")
    public String showWord(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            Word wordarray = (Word) session.getAttribute("word");
            model.addAttribute("GivenHints", wordarray.getHints());
            model.addAttribute("GivenImage", wordarray.getImage());
            model.addAttribute("user", user);
            return "word-input";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/getWord")
    public String login(@RequestParam String word1,
                        @RequestParam String word2,
                        @RequestParam String word3,
                        @RequestParam String word4,
                        @RequestParam String word5,
                        HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            Word wordarray = (Word) session.getAttribute("word");
            Integer attempts = (Integer) session.getAttribute("attempts");

            if (attempts == null) {
                attempts = 0;
            }

            model.addAttribute("GivenHints", wordarray.getHints());
            model.addAttribute("GivenImage", wordarray.getImage());

            String concatenatedWord = word1 + word2 + word3 + word4 + word5;

            if (concatenatedWord.equalsIgnoreCase(wordarray.getWordName())) {
                model.addAttribute("message1", "Congratulations You win");
                model.addAttribute("user", user);
                Integer score = Math.toIntExact(user.getScore() + 10);
                return "redirect:/Score/" + user.getId() + "/" + score;
            } else {
                attempts++;
                session.setAttribute("attempts", attempts);

                if (attempts >= 3) {
                    session.setAttribute("attempts", 0); // Reset attempts for next game
                    model.addAttribute("message", "Sorry! You lose. Moving to the next word.");
                    return "redirect:/nextWord";
                } else {
                    model.addAttribute("message", "Sorry!! You lose. You have " + (3 - attempts) + " attempts remaining.");
                    model.addAttribute("user", user);
                    return "word-input";
                }
            }
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/nextWord")
    public String getNextWord(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        String selectedLevel = (String) session.getAttribute("selectedLevel");
        redirectAttributes.addFlashAttribute("message", "Sorry! You lose. Try the next question.");
        if (user != null && selectedLevel != null) {
            Word word = wordRepository.findRandomWordByLevel(selectedLevel);
            model.addAttribute("message", "Sorry! You lose. Try the next question.");
            session.setAttribute("word", word);
            return "redirect:/showWord";
        } else {
            return "redirect:/";
        }
    }
}
