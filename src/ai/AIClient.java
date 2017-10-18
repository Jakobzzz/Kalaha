package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import kalaha.*;

import java.util.Vector;

class Node 
{
    public int key;
    public int ambo;
    public GameState board;
    public Vector<Node> children;
}

/**
 * This is the main class for your Kalaha AI bot. Currently
 * it only makes a random, valid move each turn.
 * 
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;
    
    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;
    
    //Variables for tree
    private Node m_root;
    
    /**
     * Creates a new client.
     */
    public AIClient()
    {
        //Init root
        this.m_root = null;      
        
	player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();
	
        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
            return;
        }
    }
    
    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }
    
    /**
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());
        
        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));
        
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
    
    /**
     * Adds a text string to the GUI textarea.
     * 
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
    
    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;
        
        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                    addText("I am player " + player);
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);
                            
                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;
                            
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }
                
                //Wait
                Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            running = false;
        }
        
        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
        }
    }
    
    /**
     * This is the method that makes a move each time it is your turn.
     * Here you need to change the call to the random method to your
     * Minimax search.
     * 
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */
    public int getMove(GameState currentBoard)
    {
        //Build to depth level 2
        for(int i = 0; i < 5; i++)
        {
            BuildTree(currentBoard.clone());
        }
        
        MinMax(m_root);
        m_root.key = m_root.children.get(m_root.children.size() - 1).key;
                       
        //Now compare the keys of the children of the root
        //to see which child has the proper index
        int index = 0;
                
        for(int p = 0; p < m_root.children.size(); p++)
        {
            if (m_root.board.getNextPlayer() == 1) 
            {
                int playerOneScore = m_root.board.getScore(1);
                int playerTwoScore = m_root.board.getScore(2);

                if (playerOneScore > playerTwoScore) 
                {
                    if (m_root.children.get(p).key < m_root.key) 
                    {
                        m_root.key = m_root.children.get(p).key;
                    }
                } 
                else if (playerOneScore < playerTwoScore) 
                {
                    if (m_root.children.get(p).key > m_root.key) 
                    {
                        m_root.key = m_root.children.get(p).key;
                    }
                }
                else 
                {
                    m_root.key = m_root.children.get(p).key;
                }
            }
            if (m_root.board.getNextPlayer() == 2) 
            {
                int playerOneScore = m_root.board.getScore(1);
                int playerTwoScore = m_root.board.getScore(2);

                if (playerOneScore < playerTwoScore) 
                {
                    if (m_root.children.get(p).key < m_root.key) 
                    {
                        m_root.key = m_root.children.get(p).key;
                    }
                } 
                else if (playerOneScore > playerTwoScore) 
                {
                    if (m_root.children.get(p).key > m_root.key) 
                    {
                        m_root.key = m_root.children.get(p).key;
                    }
                } 
                else 
                {
                    m_root.key = m_root.children.get(p).key;
                }
            }
            
            if(m_root.key == m_root.children.get(p).key)
            {
                index = m_root.children.get(p).ambo;
            }                 
        }      
                
       //Reset the tree        
       m_root = null; 
       return index;
    }
    
    /**
     * Returns a random ambo number (1-6) used when making
     * a random move.
     * 
     * @return Random ambo number
     */
    public int getRandom()
    {
        return 1 + (int)(Math.random() * 6);
    }
    
    private int MinMax(Node node)
    {
        //Go through the tree with DFS 
        //and propagate maximizer/minimizer
        //upwards in the tree
        for(int i = 0; i < node.children.size(); i++)
        {
            if(node.children.size() > 0)
            {
                if (node.board.getNextPlayer() == 1)
                {
                    int playerOneScore = node.board.getScore(1);
                    int playerTwoScore = node.board.getScore(2);
                    
                    if (playerOneScore > playerTwoScore)
                    {
                        if (node.key < MinMax(node.children.get(i)))
                            node.key = MinMax(node.children.get(i));                       
                    }
                    else if (playerOneScore < playerTwoScore)
                    {
                        if (node.key > MinMax(node.children.get(i)))
                            node.key = MinMax(node.children.get(i));                             
                    }
                    else
                    {
                        node.key = MinMax(node.children.get(i));
                    }
                }               
                if (node.board.getNextPlayer() == 2)
                {
                    int playerOneScore = node.board.getScore(1);
                    int playerTwoScore = node.board.getScore(2);
                    
                    if (playerOneScore < playerTwoScore)
                    {
                        if (node.key < MinMax(node.children.get(i)))
                            node.key = MinMax(node.children.get(i));                       
                    }
                    else if (playerOneScore > playerTwoScore)
                    {
                        if (node.key > MinMax(node.children.get(i)))
                            node.key = MinMax(node.children.get(i));                             
                    }
                    else
                    {
                        node.key = MinMax(node.children.get(i));
                    }
                }               
            }         
        }
        
        return node.key;     
    }
    
    private void BuildTree(GameState currentBoard)
    {
        //If the root doesn't have any children,
        //then we want to add children
        if(m_root == null)
        {
            m_root = new Node();
            m_root.key = 0;
            m_root.board = currentBoard;
            m_root.children = InsertChildren(m_root.board);
        }
        else
        {
            //Build the tree from the root and down
            Insert(m_root.board, m_root);
        }       
    }
    
    //Recursively inserts nodes into the tree
    private void Insert(GameState currentBoard, Node node)
    {
        for(int i = 0; i < node.children.size(); i++)
        {
            if(!node.children.get(i).children.isEmpty())
                Insert(node.children.get(i).board, node.children.get(i));
            else
                node.children.get(i).children = InsertChildren(node.children.get(i).board);
        }
    }
    
    //Return a vector of children which can be attached to a node
    private Vector<Node> InsertChildren(GameState currentBoard)
    {
        Vector<Node> children = new Vector<Node>();
        
        if(currentBoard.getNextPlayer() == 1)
        {
            for(int i = 1; i < 7; i++)
            {
                //Insert new child from valid game state
                GameState parentBoard = currentBoard.clone();
            
                if(parentBoard.moveIsPossible(i))
                {
                    parentBoard.makeMove(i);
                    Node node = new Node();
                    node.key = parentBoard.getScore(1);
                    node.ambo = i;
                    node.board = parentBoard;
                    node.children = new Vector<Node>();
                    children.add(node);                
                }
            }           
        }
        
        if(currentBoard.getNextPlayer() == 2)
        {
            for(int i = 1; i < 7; i++)
            {
                //Insert new child from valid game state
                GameState parentBoard = currentBoard.clone();
            
                if(parentBoard.moveIsPossible(i))
                {
                    parentBoard.makeMove(i);
                    Node node = new Node();
                    node.key = parentBoard.getScore(2);
                    node.ambo = i;
                    node.board = parentBoard;
                    node.children = new Vector<Node>();
                    children.add(node);                
                }
            }           
        }
        
        return children;     
    }
}