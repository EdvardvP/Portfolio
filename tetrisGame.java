

import java.awt.event.ActionEvent;

import java.awt.*;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Array;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;


public class tetrisGame extends JPanel implements KeyListener{
	public tetrisGame gameField;
	public int squareSize = 5;
	static int sqrSiz = 25;

	static boolean startedTimer = false;
	static boolean startedSideTimer = false;
	static boolean startedClearTimer = false;
	static boolean spawnNewBlock = true;
	static boolean lostGame = false;
	static boolean restartTimer = false;
	static boolean canSide = true;
	static boolean clearedRow = true;
	String lastLocation = "";
	static int moveSpeed = 500; //lower is faster, fallspeed
	static int sideSpeed = 60; 
	static int clearSpeed = 5;//time between each block is cleared away, when a row is cleared
	static int areaSizeX = 12;
	static int areaSizeY = 17;
	static int playfieldWidth = sqrSiz*(areaSizeX+2);
	static int playfieldHeight = sqrSiz*(areaSizeY+2);
	static int score = 0;
	static int blockCount = 0;//orders the blocks
	static int activeTileX;
	static int moveDirection = 0;
	static int bannedLeft = 0;
	static int bannedRight = 0;
	static int currentRotation = 1;
	static int currentBlockType = 0;
	static int stopTimer = 0;
	static int [] banList = new int[1000000];//can't play more then a million blocks!
	static int[][] playingField = new int[areaSizeX][areaSizeY];//0,0 is starting points
	static String[][] rotateField = new String[areaSizeX][areaSizeY]; 
	String str;
	static Timer worldTime;
		public tetrisGame(){
			this.addKeyListener(this);
		}
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			g.setColor(Color.BLUE);
			drawBlocks(g);
			g.fillRect(0, 0, playfieldWidth, sqrSiz);//top bar
			g.fillRect(0, (playfieldHeight-sqrSiz), playfieldWidth, sqrSiz);//bottom bar
			g.fillRect(0, (sqrSiz*4), playfieldWidth, sqrSiz);//middle bar
			g.fillRect(0, 0, sqrSiz, playfieldHeight);//left bar
			g.fillRect(playfieldWidth-sqrSiz, 0, sqrSiz, playfieldHeight);//right bar
			g.setColor(Color.WHITE);
			//g.fillRect(sqrSiz, sqrSiz*5, playfieldWidth-sqrSiz*2, playfieldHeight-sqrSiz*6);//background
			g.setColor(Color.blue);
			//if(lostGame)lostGame(g);
			g.setFont(new Font("Verdana", Font.BOLD,sqrSiz));
			if(!lostGame)g.drawString("Tretris, by me",2*sqrSiz,(areaSizeY*sqrSiz)/7);
			if(!lostGame)g.drawString("Score: "+score,2*sqrSiz,(areaSizeY*sqrSiz)/5);
			else {
				g.setColor(Color.RED);
				g.fillRect(sqrSiz, sqrSiz*5, playfieldWidth-sqrSiz*2, playfieldHeight-sqrSiz*6);//background
				g.setColor(Color.WHITE);
				g.setFont(new Font("Verdana", Font.ITALIC,sqrSiz));
				g.drawString("Tretris, by EEEE",2*areaSizeX,(areaSizeY*sqrSiz)/7);
				g.drawString("YOU LOST",2*areaSizeX,(areaSizeY*sqrSiz)/2);
				g.drawString("Final Score: "+score,2*sqrSiz,(areaSizeY*sqrSiz));
				
			}
			
			if(!lostGame)g.drawString("SCORE:"+score, squareSize, squareSize);
			
			
		
		}
		
		public static void main(String[] args){
			JFrame framu = new JFrame();
			framu.setSize((areaSizeX+2)*sqrSiz+sqrSiz/2,((areaSizeY+4)*sqrSiz-sqrSiz/2));//sqrSiz/2 patches a wierd padding effect
			framu.setLocation(0, 0);
			framu.setTitle("Tretris");
			framu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			framu.setVisible(true);
			tetrisGame maa = new tetrisGame();
			framu.add(maa);
			maa.requestFocus();
			
			
		}
	
		
	
		public void keyPressed(KeyEvent e) {
			
			Timer timer = new Timer(moveSpeed, new ActionListener() {//move
				  public void actionPerformed(ActionEvent arg0) {
					if(!lostGame){
						//++snakeLength;
						newBlock();
						moveBlock(false, 0);//not to the sides, not to any side
						repaint();
						
					}
				  }
				  
				});
				timer.setRepeats(true); 
				if(!startedTimer)timer.start();
				startedTimer = true;
			Timer sideMovementTimer = new Timer(sideSpeed, new ActionListener() {
					  public void actionPerformed(ActionEvent arg0) {
						if(!lostGame){						
							moveDirection = 0;
							canSide = true;
							
						}else{
							//lostGame();
						}
					  }
					  
					});
					sideMovementTimer.setRepeats(true); 
					if(!startedSideTimer)sideMovementTimer.start();
					startedSideTimer = true;
		
			if((e.getKeyCode()==68||e.getKeyCode()==39)){//d, right
				moveDirection = 1;				
				moveBlock(true,1);
				repaint();
			}
			if((e.getKeyCode()==65||e.getKeyCode()==37)){//a, left
				moveDirection = 2;				
				moveBlock(true,2);
				repaint();
			}
			if((e.getKeyCode()==87||e.getKeyCode()==38)){//w, up. rotates clockwise
				//rotate active block
				
				rotateBlock(currentBlockType,currentRotation);				
				if(currentRotation==5)currentRotation = 1;
				System.out.println(currentRotation);
				repaint();
			}
			if((e.getKeyCode()==83||e.getKeyCode()==40)){//s, down. goes down faster
				moveBlock(false,0);
				repaint();
			}
			if(e.getKeyCode()==10){//enter
				restartGame();
				
			}
			if(restartTimer){
				//timer.stop();
				//timer.restart();
				//restartTimer = false;
			}
			
			
		}
		
		
		
		public void keyReleased(KeyEvent arg0) {
			bannedRight = 0;
			bannedLeft = 0;
			
		}


		
		public void keyTyped(KeyEvent arg0) {
			
			
		}
		
		public void moveBlock(boolean move, int dir){
			//System.out.println("moving");
		if(!move){	
			for(int c = 0; c<areaSizeX; c++){
				for(int r = areaSizeY-1; r>0; r--){
					
					
					if(playingField[c][r]>=1){
						if(r==areaSizeY-1){
							if(playingField[c][r]==blockCount&&banList[blockCount]==0){
								banList[blockCount] = 1;
								System.out.println("BANNED ALL "+blockCount+"'s since at the bottom");
								spawnNewBlock = true;								
								break;		
							}
						}
						else if (playingField[c][r+1]!=0) {
							if(playingField[c][r]==blockCount&&banList[blockCount]==0&&playingField[c][r+1]!=blockCount){
								banList[blockCount] = 1;
								System.out.println("BANNED ALL "+blockCount+"'s since "+playingField[c][r+1] + ">=1 at"+c+":"+r );
								if(r==4)lostGame=true;
								else spawnNewBlock = true;								
								//break;		
							}
						}
						
							
					}else{//blank air
						
					}
					
					
				}
			}
			//so, now it has banned some. is it time to move? check to see if the selected block is banned!
			int acceptedOnes = 0;//no block is bigger then 5, so no need to run through the loop more then that
			
			for(int c = 0; c<areaSizeX; c++){
				int i = 0;
				for(int r = areaSizeY-2; r>0; r--){
					i++;
					if(banList[playingField[c][r]]!=0){
						//found a BANNED block! don't move it!
					}else{//found an accepted block OR blank nothing.
						if(playingField[c][r]>=1){
							playingField[c][r+1] = playingField[c][r];
							playingField[c][r] = 0;
							/*++acceptedOnes;
							if(acceptedOnes==5)break;*/
						}
					}
					
				}
			}
			//time to move sideways!
		}
			if(move&&canSide){
				canSide = false;
				int leftRight = 0;
				if(dir == 1){//move to the right
					leftRight = 1;
					
				}else if(dir == 2){//right
					leftRight = -1;
				}
				int cantMove = 0;
				//all parts must be next to the same blockList (itself) or nothing, else something is in the way, somewhere! go through array left-right, top to down
				for(int c = 0; c<areaSizeX; c++){
					for(int r = 0; r<areaSizeY-1; r++){
						if(playingField[c][r]>=1){
							if(dir == 1){//right
								if(c==areaSizeX-1){
									if(playingField[c][r]==blockCount){//&&banList[blockCount]==0
									//	collision with wall detected	
										++cantMove;
										
									}
								}
							}else if (dir == 2){
								if(c==0){
									if(playingField[c][r]==blockCount){
										//collision with wall detected											
										++cantMove;
									}
								}
							}
							if (c!=0&&c!=areaSizeX-1&&playingField[c+leftRight][r]!=0&&playingField[c+leftRight][r]!=blockCount) {
								System.out.println("Found collission FIRST"+leftRight);
								if(playingField[c][r]==blockCount&&banList[blockCount]==0&&playingField[c+leftRight][r]!=blockCount){
									if(dir==1)bannedRight = blockCount;
									if(dir==2)bannedLeft = blockCount;
									//found collision!
									System.out.println("Found collission "+leftRight);
									++cantMove;
									
								}
							}
							
								
						}else{//blank air
							
						}
					}
				}
				//alright, now we now if we can move or not! lets!
				if(dir==1){	//right
					for(int c = areaSizeX-1; c>=0; c--){
						for(int r = areaSizeY-1; r>0; r--){//this order makes sure it doesnt move a square it just moved
						
							if(banList[playingField[c][r]]!=0||cantMove>0){
								//found a BANNED block! don't move it!
								//System.out.println("DONT");
							}else{//found an accepted block OR blank nothing.
								if(playingField[c][r]>=1&&playingField[c+leftRight][r]==0&&bannedRight==0){
									playingField[c+leftRight][r] = playingField[c][r];
									playingField[c][r] = 0;
									cantMove = 0;
									System.out.println("DO");
									/*++acceptedOnes;
									if(acceptedOnes==5)break;*/
								}
							}							
						}
					}
				}else if(dir == 2){
					for(int c = 0; c<areaSizeX; c++){
						for(int r = areaSizeY-1; r>0; r--){//this order makes sure it doesnt move a square it just moved
							
							if(banList[playingField[c][r]]!=0||cantMove>0){
								//found a BANNED block! don't move it!
								//System.out.println("DONT LEFT");
							}else{//found an accepted block OR blank nothing.
								if(playingField[c][r]>=1&&playingField[c+leftRight][r]==0&&bannedLeft==0){
									playingField[c+leftRight][r] = playingField[c][r];
									playingField[c][r] = 0;
									cantMove = 0;
									System.out.println("DO LEFT");
									/*++acceptedOnes;
									if(acceptedOnes==5)break;*/
								}
							}
						}
					}
				}
			}
		}
			
		
		
		
		public void drawBlocks(Graphics g){
			for(int c = 0; c<areaSizeX; c++){
				for(int r = 0; r<areaSizeY; r++){
					if(playingField[c][r]>=1){
						int red = 0,green = 0,blue = 0;//we want red, green, purple and yellow ones
						//so, three prime factors, one other. 2,3,5, all other
						if(playingField[c][r]%2==0)red = 255;
						else if(playingField[c][r]%3==0)green = 255;
						else if(playingField[c][r]%5==0){
							red = 255;
							green = 255;
						}
						else{
							red = 150;
							blue = 150;
						}
						Color rgb = new Color(red,green,blue);
						g.setColor(rgb);
						//System.out.println(playingField[c][r]);
						g.fillRect(sqrSiz*c+sqrSiz, sqrSiz*r+sqrSiz, sqrSiz, sqrSiz);						
						g.setColor(Color.BLUE);
					}
				}
			}
		}
		
		public void newBlock(){
			if(spawnNewBlock&&clearedRow){
				detectRow();
				currentRotation = 1;
				int type = (int) (Math.ceil(Math.random()*2));
				currentBlockType = type;
				placeBlock(type);
				spawnNewBlock = false;
			}
		}
		
		public void placeBlock(int type){
			int startX = areaSizeX/2;
			activeTileX = startX;
			++blockCount;
			//System.out.println("blockcount: "+blockCount);
			switch(type){
			case 1://t-shape, facing down
				playingField[startX][4] = blockCount;
				playingField[startX+1][4] = blockCount;
				playingField[startX+2][4] = blockCount;
				playingField[startX+1][5] = blockCount;				
				break;
			case 2://L with left ass
				playingField[startX][4] = blockCount;
				playingField[startX][5] = blockCount;
				playingField[startX][6] = blockCount;
				playingField[startX-1][6] = blockCount;
				break;
			case 3://straight
				playingField[startX][4] = blockCount;
				playingField[startX][5] = blockCount;
				playingField[startX][6] = blockCount;
				playingField[startX][7] = blockCount;
				break;
			case 4://n, bottom-top
				playingField[startX][4] = blockCount;
				playingField[startX+1][4] = blockCount;
				playingField[startX+1][5] = blockCount;
				playingField[startX+2][5] = blockCount;
				break;
			case 5://n, top-bottom
				playingField[startX][5] = blockCount;
				playingField[startX+1][5] = blockCount;
				playingField[startX+1][4] = blockCount;
				playingField[startX+2][4] = blockCount;
				break;
				
			default: //L-shape, right ass				
				playingField[startX][4] = blockCount;
				playingField[startX][5] = blockCount;
				playingField[startX][6] = blockCount;
				playingField[startX+1][6] = blockCount;
				break;
			}
		
			//int startX = (int) (Math.floor(Math.random()*(areaSizeX-2)));
			
		}
		
		public void rotateBlock(int blockID,int currentRotation){//only allow rotation if it does not interfere
			switch(blockID){
				case 1: //t-shape			
					switch(currentRotation){
						case 1://facing down - go to left
							rotateTShape(2,0,1,-1);
							break;
						case 2: //facing left				
							rotateTShape(1,1,2,0);
							break;
						case 3: //facing up				
							rotateTShape(0,0,1,1);
							break;
						default: //facing right				
							rotateTShape(0,0,-1,1);
							break;
					}
					break;
				
				case 2: //L-shape, left ass
					switch(currentRotation){
						case 1://standing up
							rotateLShape(0,0,0,-2,1,0,0,-1,1,-2,2,-1);
							break;
						case 2: //facing left				
							rotateLShape(0,0,2,0,0,1,1,0,2,1,1,2);
							break;
						case 3: //facing up				
							rotateLShape(0,0,-1,1,1,0,1,1,0,2,1,2);
							break;
						default: //facing right				
							rotateLShape(0,0,0,1,2,0,1,-1,2,1,1,1);
							break;
					}
					break;
					
			}
		}
		
		public void rotateTShape(int offx1, int offy1, int offx2, int offy2){
			//find where the block is. then, move the three pieces that are not in the center. technically, move just the right-most piece to the top
			//then, find the desired piece's x and y chords!
			boolean breakOut = false;
			for(int c = 0; c<areaSizeX; c++){
				for(int r = 0; r<areaSizeY; r++){// (: ahh, re-using code feels goood
					if(c+offx2<areaSizeX&&c+offx2>=0){//no rotation out of bounds!
					 if(playingField[c][r]==blockCount&&playingField[c+offx2][r+offy2]==0){//finds the top-most,left-most piece, then navigates from there
						 playingField[c+offx1][r+offy1] = 0;
						 playingField[c+offx2][r+offy2] = blockCount;//rotation is done!
						 breakOut = true;
						 ++currentRotation;
						 break;
					 }
					}
				}
				if(breakOut)break;
			}
		}
		
		public void rotateLShape(int remx1, int remy1, int addx1, int addy1,int remx2, int remy2, int addx2, int addy2,int remx3, int remy3, int addx3, int addy3){
			boolean breakOut = false;
			for(int c = 0; c<areaSizeX; c++){
				for(int r = 0; r<areaSizeY; r++){//duplicate code again!? ):
					if(c+addx1<areaSizeX&&c+addx1>=0&&c+addx2<areaSizeX&&c+addx2>=0&&c+addx3<areaSizeX&&c+addx3>=0){//makes sure you cant rotate out of bounds
					 if(playingField[c][r]==blockCount&&playingField[c+addx1][r+addy1]==0&&playingField[c+addx2][r+addy2]==0&&playingField[c+addx3][r+addy3]==0){//finds the top-most,left-most piece, then navigates from there
						 playingField[c+remx1][r+remy1] = 0;
						 playingField[c+addx1][r+addy1] = blockCount;//moved one piece, out of 3
						 playingField[c+remx2][r+remy2] = 0;
						 playingField[c+addx2][r+addy2] = blockCount;//moved 2 pieces, out of 3
						 playingField[c+remx3][r+remy3] = 0;
						 playingField[c+addx3][r+addy3] = blockCount;//moved 3 pieces, out of 3
						 breakOut = true;
						 ++currentRotation;
						 break;
					 }
					}
				}
				if(breakOut)break;
			}
		}
		
		public void detectRow(){
			int rowFound = 0;
			int rowsFound = 0;
			for(int r = 0; r<areaSizeY; r++){//find rows on X-AXIS, not Y!!
				int rowCount = 0;
				for(int c = 0; c<areaSizeX; c++){
					if(playingField[c][r]!=0)rowCount++;//found a block
					if(rowCount==areaSizeX){
						++rowsFound;
						clearedRow = false;
						rowFound = r;
						for(int c1 = 0; c1<areaSizeX; c1++){
							playingField[c1][r] = 0;
							score = score + 100;	
							
							
						}	
						System.out.println("CLEARED ROW! Score: "+score);
						/*for(int c1 = 0; c1<banList.length; c1++){
							if(banList[c1]!=0){				
								banList[c1] = 0;
											
							}else break;	
						}*/
						clearedRow = true;
						break;
					}
				}
			}
			//now, move everything down
			for(int rows = 0; rows<rowsFound;rows++){
				for(int c = 0; c<areaSizeX; c++){			
					for(int r = rowFound; r>0; r--){
						if(playingField[c][r]!=0){
							playingField[c][r+1] = playingField[c][r];
							playingField[c][r] = 0;
						}
					}
				
				}
				
				
			}
			spawnNewBlock = true;
			System.out.println("Spawn new");
		}
		
		public void restartGame(){
			for(int c = 0; c<areaSizeX; c++){
				for(int r = 0; r<areaSizeY; r++){
					playingField[c][r] = 0;
				}
			}
			for(int c = 0; c<banList.length; c++){
				banList[c] = 0;
			}
			score = 0;
			blockCount = 0;
			currentRotation = 1;
			currentBlockType = 0;
			spawnNewBlock = true;
			lostGame = false;
			
		}
	
}
	
		
	


