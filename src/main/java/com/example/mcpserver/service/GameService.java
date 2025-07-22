package com.example.mcpserver.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * GameService - Provides game-related tools for MCP
 *
 * @author mcp-server
 */
@Service
public class GameService {

    private final Random random = new Random();

    @Tool(description = "Play Rock, Paper, Scissors - randomly returns one of the three options")
    public String rockPaperScissors() {
        String[] options = {"rock", "paper", "scissors"};
        String result = options[random.nextInt(options.length)];
        return "Computer chose: " + result;
    }

    @Tool(description = "Play Rock, Paper, Scissors against a computer - you choose your move")
    public String playRockPaperScissors(@ToolParam(description = "Your choice: rock, paper, or scissors") String playerChoice) {
        String[] options = {"rock", "paper", "scissors"};
        String computerChoice = options[random.nextInt(options.length)];
        
        // Normalize player choice
        String normalizedPlayerChoice = playerChoice.toLowerCase().trim();
        
        if (!normalizedPlayerChoice.equals("rock") && 
            !normalizedPlayerChoice.equals("paper") && 
            !normalizedPlayerChoice.equals("scissors")) {
            return "Invalid choice! Please choose rock, paper, or scissors.";
        }
        
        String result = determineWinner(normalizedPlayerChoice, computerChoice);
        return String.format("You chose: %s, Computer chose: %s. %s", 
                           normalizedPlayerChoice, computerChoice, result);
    }

    @Tool(description = "Get a random choice from Rock, Paper, Scissors")
    public String getRandomChoice() {
        String[] options = {"rock", "paper", "scissors"};
        String result = options[random.nextInt(options.length)];
        return "Random choice: " + result;
    }

    /**
     * Determine the winner of a Rock, Paper, Scissors game
     */
    private String determineWinner(String playerChoice, String computerChoice) {
        if (playerChoice.equals(computerChoice)) {
            return "It's a tie!";
        }
        
        if ((playerChoice.equals("rock") && computerChoice.equals("scissors")) ||
            (playerChoice.equals("paper") && computerChoice.equals("rock")) ||
            (playerChoice.equals("scissors") && computerChoice.equals("paper"))) {
            return "You win!";
        } else {
            return "Computer wins!";
        }
    }
} 