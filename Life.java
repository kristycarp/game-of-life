//Kristy Carpenter, Computer Science II, Fall 2014, Section C (4th period)
//Assignment 7--Game of Life Option
//
//This program runs a simulation of Conway's Game of Life. It takes intput from the user through
//the console to determine the input file, output file, number of frames to run, and time between
//steps to use, and uses try/catch statements to ensure that no input will cause errors later on.
//Then, arrays are used to determine the next generation, which is depicted on the DrawingPanel.
//After the set number of frames go by, the simulation stops, and the program uses a PrintStream to
//print the final generation into the output file.
//There is an extension that can be enabled or disabled that shows how long a cell has been alive
//through color (more purple = older)

import java.awt.*; //for drawing panel
import java.util.*; //for scanner
import java.io.*; //for file input

public class Life
{
   /**
     *this constant controls the size of each "pixel" in pixels
     */
   public static final int PIXEL_SIZE = 10;
   
   /**
     *when true, this triggers the extra functionality of colors changing as a pixel "ages"
     */
   public static final boolean COLOR_AGE_INDICATOR = false;
   
   /**
     *the main method, where the program begins
     *
     *@param args
     */
   public static void main(String[] args)
   {
      /**char[][] initialWorld = {{'.', 'X', '.'},
                               {'.', 'X', '.'},
                               {'.', 'X', '.'}};**/
      String[] userInput = introAndPrompt();
      char[][][] initialWorld = processFile(userInput[0]);
      if (initialWorld != null)
      {
         PrintStream output = makeAPrintStream(userInput[1]);
         int nFrames = Integer.parseInt(userInput[2]);
         int stepTime = Integer.parseInt(userInput[3]);
         DrawingPanel panel = new DrawingPanel(PIXEL_SIZE * initialWorld[0].length, PIXEL_SIZE * initialWorld.length);
         //panel.setBackground(Color.GRAY); //for testing only
         Graphics g = panel.getGraphics();
         drawWorld(initialWorld, null, g); //draw first frame
         int frameCount = 1; //first frame has been drawn
         while (frameCount != nFrames) 
         {
            panel.sleep(stepTime); 
            frameCount++;
            char[][][] nextWorld = makeNextGeneration(initialWorld);
            drawWorld(nextWorld, initialWorld, g);
            initialWorld = nextWorld; 
         }
         //System.out.println(Arrays.deepToString(nextWorld)); //for testing only
         makeOutputFile(initialWorld, output);
         System.out.println("Simulation successful!");
      }
   }
   
   /**
     *this method prints out the introduction and prompts the user for input. It also makes sure
     *that all input is robust before returning it all to the main method in an array. It will
     *reprompt the user until they enter acceptable data.
     *
     *@return allInput - the four inputs from the user, none of which will cause errors later on
     */
   public static String[] introAndPrompt()
   {
      Scanner input = new Scanner(System.in);
      String[] allInput = new String[4];
      boolean fileGood = false;
      boolean outputGood = false;
      boolean framesGood = false;
      boolean steptimeGood = false;
      System.out.println("This program runs Conway's Game of Life");
      if (COLOR_AGE_INDICATOR)
      {
         System.out.println("Color changing functionality is on:");
         System.out.println("The longer a cell is alive, the more purple it will get.");
      }
      System.out.print("Input file name? ");
      while (!fileGood)
      {
         allInput[0] = input.nextLine();
         String filename = allInput[0];
         try
         {
            File f = new File(filename);
            Scanner test = new Scanner(f);
            fileGood = true;
         }
         catch (FileNotFoundException e)
         {
            System.out.print("File not found. Try again: ");
            fileGood = false;
         }
      }
      while (!outputGood)
      {
         System.out.print("Output file name? ");
         allInput[1] = input.nextLine();
         String outputFile = allInput[1];
         try
         {
            File f = new File(outputFile);
            PrintStream test = new PrintStream(f);
            outputGood = true;
         }
         catch (FileNotFoundException e)
         {
            System.out.print("Error creating file. Try again: ");
            outputGood = false;
         }
      }
      while (!framesGood)
      {
         System.out.print("Number of frames to run the simulation: ");
         allInput[2] = input.nextLine();
         try
         {
            int nFrames = Integer.parseInt(allInput[2]);
            framesGood = true;
         }
         catch (NumberFormatException i)
         {
            System.out.println("Input is not valid, you need to enter a number");
            framesGood = false;
         }

      }
      while (!steptimeGood)
      {
         System.out.print("Time between steps (ms): ");
         allInput[3] = input.nextLine();
         try
         {
            int steps = Integer.parseInt(allInput[3]);
            steptimeGood = true;
         }
         catch (NumberFormatException i)
         {
            System.out.println("Input is not valid, you need to enter a number");
            steptimeGood = false;
         }
      }
      return allInput;
   }
   
   /**
     *this method takes the file given by the user, reads in the data, and converts it into a
     *2D array for use later in the program. If there was an error in the file's format, it stops
     *the program (but without throwing an exception).
     *
     *@param filename - the name of the file to scan
     *@return array - the 2D array with the initial state of the world indicated in the file
     */
   public static char[][][] processFile(String filename)
   {
      Scanner fileScan = null;
      File file = null;
      try
      {
         file = new File(filename);
         fileScan = new Scanner(file);
      }
      catch (FileNotFoundException e) //this shouldn't happen because file was already tested
      {
         System.out.print("File not found. Try again: ");
         return null;
      }
      int nRows = fileScan.nextInt();
      int nCols = fileScan.nextInt();
      fileScan.nextLine(); //finish off that line
      char[][][] array = new char[nRows][nCols][2];
      for (int row = 0; row < array.length; row++)
      {
         String rowData = fileScan.nextLine();
         for (int col = 0; col < array[row].length; col++)
         {
            try
            {
               array[row][col][0] = rowData.charAt(col);
            }
            catch (StringIndexOutOfBoundsException wrongFormatMakesMeSad)
            {
               System.out.println("Error found in the input file. Halting simulation.");
               return null;
            }
         }
      }
      return array;
   }
   
   /**
     *this method creates a printstream given the name of an output file
     *
     *@param filename - the name of the output file
     *@return output - the printstream which can print into the output file
     */
   public static PrintStream makeAPrintStream(String filename)
   {
      PrintStream output = null;
      File file = null;
      try
      {
         file = new File(filename);
         output = new PrintStream(file); 
      }
      catch (FileNotFoundException e)
      {
         System.out.println("Error opening file: " + filename);
         return null;
      }
      return output;
   }
   
   /**
     *this method uses the rules of Conway's Game of Life to determine if each cell is alive or
     *dead in a given world, and then creates a new 2D array with the next generation of the world
     *
     *@param initialWorld - the world to process
     *@return aWholeNewWorld - the next generation of the world
     */
   public static char[][][] makeNextGeneration(char[][][] initialWorld)
   {
      char[][][] aWholeNewWorld = new char[initialWorld.length][initialWorld[0].length][2];
      for (int row = 0; row < initialWorld.length; row++)
      {
         for (int col = 0; col < initialWorld[row].length; col++)
         {
            int aliveNeighbors = 0;
            if (row != 0)
            {
               if ((col != 0) && (initialWorld[row - 1][col - 1][0] == 'x'))
               {
                  aliveNeighbors++;
               }
               if (initialWorld[row - 1][col][0] == 'x')
               {
                  aliveNeighbors++;
               }
               if ((col != initialWorld[0].length - 1) && (initialWorld[row - 1][col + 1][0] == 'x'))
               {
                  aliveNeighbors++;
               }
            }
            if (row != initialWorld.length - 1)
            {
               if ((col != 0) && (initialWorld[row + 1][col - 1][0] == 'x'))
               {
                  aliveNeighbors++;
               }
               if (initialWorld[row + 1][col][0] == 'x')
               {
                  aliveNeighbors++;
               }
               if ((col != initialWorld[0].length - 1) && (initialWorld[row + 1][col + 1][0] == 'x'))
               {
                  aliveNeighbors++;
               }
            }
            if ((col != 0) && (initialWorld[row][col - 1][0] == 'x'))
            {
               aliveNeighbors++;
            }
            if ((col != initialWorld[0].length - 1) && (initialWorld[row][col + 1][0] == 'x'))
            {
               aliveNeighbors++;
            }
            if (initialWorld[row][col][0] == 'x')
            {
               if (aliveNeighbors < 2)
               {
                  aWholeNewWorld[row][col][0] = '.';
               }
               else if (aliveNeighbors < 4)
               {
                  aWholeNewWorld[row][col][0] = 'x';
               }
               else
               {
                  aWholeNewWorld[row][col][0] = '.';
               }
            }
            else
            {
               if (aliveNeighbors == 3)
               {
                  aWholeNewWorld[row][col][0] = 'x';
               }
               else
               {
                  aWholeNewWorld[row][col][0] = '.';
               }
            }
         }
      }
      
      return aWholeNewWorld;
   }
   
   /**
     *this method takes a 2D array of a world, and prints a graphical representation of its
     *contents into the DrawingPanel.
     *
     *@param world - the world to draw
     *@param g - the graphics context for the DrawingPanel
     */
   public static void drawWorld(char[][][] newWorld, char[][][] oldWorld, Graphics g)
   {
      for (int row = 0; row < newWorld.length; row++)
      {
         for (int col = 0; col < newWorld[row].length; col++)
         {
            if (newWorld[row][col][0] == 'x')
            {
               if (COLOR_AGE_INDICATOR)
               {
                  if (oldWorld != null && oldWorld[row][col][0] == 'x')
                  {
                     newWorld[row][col][1] = oldWorld[row][col][1];
                     newWorld[row][col][1]++;
                  }
                  else
                  {
                     newWorld[row][col][1] = 'a';
                  }
                  int purpleValue = (newWorld[row][col][1] - 96) * 25;
                  if (purpleValue > 255)
                  {
                     purpleValue = 255;
                  }
                  Color color = new Color(purpleValue, 0, purpleValue);
                  g.setColor(color);
               }
               else
               {
                  g.setColor(Color.BLACK);
               }
            }
            else
            {
               g.setColor(Color.WHITE);
            }
            g.fillRect(col * PIXEL_SIZE, row * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
         }
      }
   }
   
   /**
     *this method prints the contents of the final world into an output file
     *
     *@param finalWorld - the world to print to the output file
     *@param output = the PrintStream which can print to the output file
     */
   public static void makeOutputFile(char[][][] finalWorld, PrintStream output)
   {
      for (int row = 0; row < finalWorld.length; row++)
      {
         for (int col = 0; col < finalWorld[0].length; col++)
         {
            output.print(finalWorld[row][col][0]);
         }
         output.println("");
      }
   }
}