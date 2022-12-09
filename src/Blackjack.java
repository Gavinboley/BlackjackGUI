import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

public class Blackjack extends JFrame implements ActionListener, KeyListener
{
    // Constants for the game
    private static final int DECK_SIZE = 52;
    private static final int DEALER_HIT_LIMIT = 16;
    private static final int PLAYER_HIT_LIMIT = 21;

    // Array of card images
    private Image[] cardImages = new Image[DECK_SIZE];

    // Array of card values
    final int[] CARD_VALUES = {11,2,3,4,5,6,7,8,9,10,10,10,10,11,2,3,4,5,6,7,8,9,10,10,10,10,11,2,3,4,5,6,7,8,9,10,10,10,10,11,2,3,4,5,6,7,8,9,10,10,10,10};

    final String[] CARD_FILE_NAMES = {"CA","C2","C3","C4","C5","C6","C7","C8","C9","C10","CJ","CQ","CK","SA","S2","S3","S4","S5","S6","S7","S8","S9","S10","SJ","SQ","SK","DA","D2","D3","D4","D5","D6","D7","D8","D9","D10","DJ","DQ","DK","HA","H2","H3","H4","H5","H6","H7","H8","H9","H10","HJ","HQ","HK"};

    // List of cards in the deck
    private ArrayList<Integer> deck;

    // List of cards in the player's hand
    private ArrayList<Integer> playerHand;

    // List of cards in the dealer's hand
    private ArrayList<Integer> dealerHand;

    // Timer for animating the game
    private Timer timer;

    // Current game state
    private boolean gameOver;
    private boolean playerTurn;
    private boolean playerStand;
    private boolean dealerTurn;
    private boolean checkedWinCond;
    private boolean firstRun;
    private String why;

    // Current player and dealer scores
    private int playerScore;
    private int dealerScore;

    // Index of the current card in the deck
    private int cardIndex;

    // Current player stats
    private int totalGames;
    private int totalWon;
    private int totalLost;
    private int totalTie;

    private Image offScreenBuffer;
    private int knownHeight = getHeight();
    private int knownWidth = getWidth();

    // Constructor
    public Blackjack()
    {
        // Initialize the game state
        initGame();

        // Start the timer
        timer = new Timer(1000, this);
        timer.start();
        addKeyListener(this);

        File folder = new File("Cardimages");
        String directory = folder.getAbsolutePath();

        // Check if the folder exists
        if (folder.exists())
        {
            loadCards(directory);
        }
        else
        {
            System.exit(0);
        }
    }
        // Load the images of the cards from the specified directory
    private void loadCards(String directory)
    {
        for (int i = 0; i < cardImages.length; i++)
        {
            // Get the file name of the image
            String fileName = CARD_FILE_NAMES[i] + ".png";
    
            // Load the image from the file
            ImageIcon icon = new ImageIcon(directory + "\\" + fileName);
            cardImages[i] = icon.getImage();
        }
    }
    public static void main(String[] args)
    {
        // Create the game window
        Blackjack game = new Blackjack();

        // Set the title of the window
        game.setTitle("Blackjack");

        // Set the size of the window
        game.setSize(1280, 720);

        // Set the default close operation of the window
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the window to be visible
        game.setVisible(true);

        game.setResizable(false);

        game.setLocationRelativeTo(null);
    }

    // Initialize the game state
    private void initGame()
    {
        // Initialize the deck and hands
        deck = new ArrayList<>(DECK_SIZE);
        playerHand = new ArrayList<>();
        dealerHand = new ArrayList<>();

        // Shuffle the deck
        shuffleDeck();

        // Deal the initial cards
        playerHand.add(deck.get(dealCard()));
        dealerHand.add(deck.get(dealCard()));
        playerHand.add(deck.get(dealCard()));
        dealerHand.add(deck.get(dealCard()));

        // Set the game state variables
        gameOver = false;
        playerTurn = true;
        playerStand = false;
        dealerTurn = false;
        checkedWinCond = false;
        firstRun = true;
        why = "default";

        // Calculate the initial player and dealer scores
        playerScore = getHandValue(playerHand);
        dealerScore = getDealerHandValueStart(dealerHand);

        // Set the card index to 0
        cardIndex = 0;
    }
    @Override
    public void paint(Graphics g)
    {
        if ((offScreenBuffer == null) || knownHeight != getHeight() || knownWidth != getWidth()) 
        {
            offScreenBuffer = createImage(getWidth(), getHeight());
        }
        Graphics gBuffer = offScreenBuffer.getGraphics();

        // Draw the background
        gBuffer.setColor(Color.BLACK);
        gBuffer.fillRect(0, 0, getWidth(), getHeight());

        // Draw the player's cards
        for (int i = 0; i < playerHand.size(); i++)
        {
            // Get the image of the card
            Image cardImage = getCardImage(playerHand.get(i));

            // Calculate the position of the card
            int x = 20 + i * 180;
            int y = 350;

            // Draw the card
            gBuffer.drawImage(cardImage, x, y, 167, 233, this);
        }

        // Draw the dealer's cards
        if(firstRun == true)
        {
            Image cardImage = getCardImage(dealerHand.get(0));
            File folder = new File("Cardimages");
            String directory = folder.getAbsolutePath();
            Image question = null;
            try {
                question = ImageIO.read(new File(directory + "\\back.png"));
            }
            catch (IOException e) {
                System.exit(0);
            }
            // Calculate the position of the card
            int x = 20;
            int y = 80;

            // Draw the card
            gBuffer.drawImage(cardImage, x, y, 167, 233, this);
            gBuffer.drawImage(question, x+180, y, 167, 233, this);
        }
        else
        {
            for (int i = 0; i < dealerHand.size(); i++)
            {
                // Get the image of the card
                Image cardImage = getCardImage(dealerHand.get(i));

                // Calculate the position of the card
                int x = 20 + i * 180;
                int y = 80;

                // Draw the card
                gBuffer.drawImage(cardImage, x, y, 167, 233, this);
            }
        }
        // Draw the player's score
        gBuffer.setColor(Color.WHITE);
        gBuffer.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        String playerScoreText = "Player: " + playerScore;
        gBuffer.drawString(playerScoreText, 20, 720 - 40);

        // Draw the dealer's score
        String dealerScoreText = "Dealer: " + dealerScore;
        gBuffer.drawString(dealerScoreText, 20, 60);

        // Draw the game over message
        if (gameOver)
        {
            why = CheckWinCondition();
            changeAceValue();
            changeDealerAceValue();
            gBuffer.setColor(Color.GRAY); 
            if(why == "You have won")
            {
                gBuffer.setColor(Color.GREEN);
            }
            else if(why == "You have lost")
            {
                gBuffer.setColor(Color.RED);
            }
            gBuffer.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
            gBuffer.drawString("Game Over!", getWidth()/ 2 - 150, 720 / 2);
            if(why == "default")
                gBuffer.drawString("The game is a tie", getWidth() / 2 - 170, 720 / 2+50);
            else
                gBuffer.drawString(why, getWidth() / 2 - 170, 720 / 2+50);
        }

        // Draw the game instructions
        gBuffer.setColor(Color.WHITE);
        gBuffer.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        gBuffer.drawString("Press H to hit, S to stand, R to Restart, Q to Quit", 20, 720 - 15);
        FontMetrics fontMetrics = gBuffer.getFontMetrics();
        NumberFormat percentageFormat = NumberFormat.getPercentInstance();
        percentageFormat.setMaximumFractionDigits(2);
        float PCT = (totalGames != 0) ? (((float)totalWon + (float)totalTie) / (float)totalGames) * 100 : 0;
        String formattedPCT = String.format("%.1f", PCT);
        int textWidth = fontMetrics.stringWidth("Total: " + totalGames + ", W: " + totalWon + ", L: " + totalLost + ", T: " + totalTie + ", W+T PCT: " + formattedPCT + "%");
        int x = (getWidth() - textWidth) - 20;
        gBuffer.drawString("Total: " + totalGames + ", W: " + totalWon + ", L: " + totalLost + ", T: " + totalTie + ", W+T PCT: " + formattedPCT + "%", x, 720 - 15);
        g.drawImage(offScreenBuffer, 0, 0, this);
        knownHeight = getHeight();
        knownWidth = getWidth();
    }
    @Override
    public void actionPerformed(ActionEvent e)
    {
        // Update the game state
        update();

        // Repaint the game area
        repaint();
    }
    // Update the game state
    private void update()
    {
        // Check if the player has won or lost
        if (playerScore > PLAYER_HIT_LIMIT)
        {
            changeAceValue();
            if(playerScore > PLAYER_HIT_LIMIT)
            {
                changeAceValue();
                if(playerScore > PLAYER_HIT_LIMIT)
                {
                    gameOver = true;
                }
            }
        }

        // Check if it's the dealer's turn
        if (dealerTurn && !gameOver)
        {
            firstRun = false;
            dealerScore = getHandValue(dealerHand);
            paint(getGraphics());
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                // Print an error message if the thread was interrupted
                System.err.println("Thread interrupted: " + e.getMessage());
            }
            if (dealerScore >= DEALER_HIT_LIMIT)
            {
                changeDealerAceValue();
                if(dealerScore >= DEALER_HIT_LIMIT)
                {
                    changeDealerAceValue();
                    dealerTurn = false;
                }
            }
            else
            {
                // Deal another card to the dealer
                dealerHand.add(dealCard());

                // Calculate the dealer's score
                if(firstRun == true)
                    dealerScore = getDealerHandValueStart(dealerHand);
                else
                    dealerScore = getHandValue(dealerHand);

                // Check if the dealer has reached the hit limit
                if (dealerScore >= DEALER_HIT_LIMIT)
                {
                    changeDealerAceValue();
                    if(dealerScore >= DEALER_HIT_LIMIT)
                    {
                        changeDealerAceValue();
                        dealerTurn = false;
                    }
                }
            }
            repaint();
        }
        if(((playerStand == true) || (playerScore >= PLAYER_HIT_LIMIT)) && dealerTurn == false)
        {
            changeAceValue();
            changeDealerAceValue();
            if(((playerStand == true) || (playerScore >= PLAYER_HIT_LIMIT)) && dealerTurn == false)
                gameOver = true;
        }
    }
    // Shuffle the deck and reset the game state
    private void shuffleDeck()
    {
        // Create a new deck of cards
        deck = new ArrayList<>();
        for (int i = 0; i < DECK_SIZE; i++)
        {
            deck.add(i);
        }

        // Shuffle the deck
        Collections.shuffle(deck);
    }

    // Deal a card from the deck
    private int dealCard()
    {
        // Get the next card in the deck
        int card = deck.get(cardIndex);

        // Increment the card index
        cardIndex++;

        return card;
    }
    // Calculate the value of the specified hand
    private int getHandValue(ArrayList<Integer> hand)
    {
        // Initialize the hand value and the number of aces
        int handValue = 0;
        int numAces = 0;

        // Calculate the hand value and the number of aces
        for (int i = 0; i < hand.size(); i++)
        {
            // Get the value of the card
            int cardValue = CARD_VALUES[hand.get(i)];

            // Add the value to the hand value
            handValue += cardValue;

            // Check if the card is an ace
            if (cardValue == 11)
            {
                numAces++;
            }
        }

        // Check if the hand contains any aces
        if (numAces > 0)
        {
            // Check if the hand value is less than or equal to 11
            if (handValue >= 11)
            {
                // Add 10 to the hand value for each ace
                handValue -= 10 * numAces;
            }
        }

        return handValue;
    }
    private int getDealerHandValueStart(ArrayList<Integer> hand)
    {
        int handValue = 0;

        int cardValue = CARD_VALUES[hand.get(0)];

        handValue += cardValue;

        return handValue;
    }

    // Get the image of the specified card
    private Image getCardImage(int cardIndex)
    {
        // Check if the card index is valid
        if (cardIndex >= 0 && cardIndex < cardImages.length)
        {
            // Return the image of the card
            return cardImages[cardIndex];
        }
        else
        {
            // If the card index is invalid, return null
            return cardImages[0];
        }
    }

    // Handle key press events
    @Override
    public void keyPressed(KeyEvent e)
    {
        repaint();
        // Get the key code of the pressed key
        int key = e.getKeyCode();

        // Check if the "H" key was pressed
        if (key == KeyEvent.VK_H)
        {
            // If it's the player's turn, deal another card to the player
            if (playerTurn && !gameOver)
            {
                playerHand.add(dealCard());
                changeAceValue();

                // Check if the player has reached the hit limit
                if (playerScore >= PLAYER_HIT_LIMIT)
                {
                    changeAceValue();
                    if (playerScore >= PLAYER_HIT_LIMIT)
                    {
                        changeAceValue();
                        if (playerScore >= PLAYER_HIT_LIMIT)
                        {
                            playerTurn = false;
                            dealerTurn = true;
                        }
                    }
                }
            }
        }

        // Check if the "S" key was pressed
        if (key == KeyEvent.VK_S)
        {
            // If it's the player's turn, end the player's turn and start the dealer's turn
            if (playerTurn && !gameOver)
            {
                playerStand = true;
                playerTurn = false;
                dealerTurn = true;
            }
        }

        // Check if the "R" key was pressed
        if (key == KeyEvent.VK_R)
        {
            if(CheckWinCondition() == "default")
            {
                totalLost += 1;
                totalGames += 1;
            }
            initGame();
        }

        // Check if the "Q" key was pressed
        if (key == KeyEvent.VK_Q)
        {
            System.exit(0);
        }
    }

    // Handle key release events
    @Override
    public void keyReleased(KeyEvent e)
    {
        // Do nothing
    }

    // Handle key type events
    @Override
    public void keyTyped(KeyEvent e)
    {
        // Do nothing
    }
    public String CheckWinCondition()
    {
        // Calculate the player's and dealer's scores
        int playerScore = getHandValue(playerHand);
        int dealerScore = getHandValue(dealerHand);

        // If the player's score is greater than 21, the player has lost
        if (playerScore > 21)
        {
            if(checkedWinCond == false)
            {
                totalGames += 1;
                totalLost += 1;
                checkedWinCond = true;
            }
            return "You have lost";
        }

        // If the dealer's score is greater than 21, the player has won
        if (dealerScore > 21)
        {
            if(checkedWinCond == false)
            {
                totalGames += 1;
                totalWon += 1;
                checkedWinCond = true;
            }
            return "You have won";
        }

        // If the player's score is greater than the dealer's score, the player has won
        if ((playerScore > dealerScore)&& gameOver == true)
        {
            if(checkedWinCond == false)
            {
                totalGames += 1;
                totalWon += 1;
                checkedWinCond = true;
            }
            return "You have won";
        }

        
        // If the dealer's score is greater than the player's score, the player has lost
        if ((dealerScore > playerScore) && gameOver == true)
        {
            if(checkedWinCond == false)
            {
                totalGames += 1;
                totalLost += 1;
                checkedWinCond = true;
            }
            return "You have lost";
        }

        // If none of the above conditions are met, the game is a tie
        if(checkedWinCond == false && gameOver == true)
        {
            totalGames += 1;
            totalTie += 1;
            checkedWinCond = true;
            return "The game is a tie";
        }
        return "default";
    }

    private void changeAceValue()
    {
        playerScore = getHandValue(playerHand);
        // Loop through the cards in the player's hand
        for (int i = 0; i < playerHand.size(); i++)
        {
            // Get the value of the current card
            int cardValue = CARD_VALUES[playerHand.get(i)];

            // Check if the card is an ace and the player's score is over 21
            if (cardValue == 11 && playerScore > 21)
            {
                // Change the value of the ace to 1
                CARD_VALUES[playerHand.get(i)] = 1;

                // Update the player's score
                playerScore = getHandValue(playerHand);
            }
        }
        playerScore = getHandValue(playerHand);
    }
    private void changeDealerAceValue()
    {
        dealerScore = getHandValue(dealerHand);
        // Loop through the cards in the player's hand
        for (int i = 0; i < dealerHand.size(); i++)
        {
            // Get the value of the current card
            int cardValue = CARD_VALUES[dealerHand.get(i)];

            // Check if the card is an ace and the player's score is over 21
            if (cardValue == 11 && dealerScore > 21)
            {
                // Change the value of the ace to 1
                CARD_VALUES[dealerHand.get(i)] = 1;

                // Update the player's score
                dealerScore = getHandValue(dealerHand);
            }
        }
        dealerScore = getHandValue(dealerHand);
    }
}
