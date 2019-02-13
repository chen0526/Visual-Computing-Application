/*
    Group 19
    Assignment1 Comp3301
    Nicholas Atkins 201509098
    Meheroon Nesa Tondra 201555661
    Yusong Du 201458700
*/

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

// Main class
public class SmoothingFilter extends Frame implements ActionListener {
	BufferedImage input;
	ImageCanvas source, target;
	TextField texSigma;
	int width, height;
	int gray_scale = 256;
	// Constructor
	public SmoothingFilter(String name) {
		super("Smoothing Filters");
		// load image
		try {
			input = ImageIO.read(new File(name));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		target = new ImageCanvas(input);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Add noise");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 mean");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Sigma:"));
		texSigma = new TextField("1", 1);
		controls.add(texSigma);
		button = new Button("5x5 Gaussian");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 median");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 Kuwahara");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+100, height+100);
		setVisible(true);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {
		// example -- add random noise
		if ( ((Button)e.getSource()).getLabel().equals("Add noise") ) {
			Random rand = new Random();
			int dev = 64;
			for ( int y=0, i=0 ; y<height ; y++ )
				for ( int x=0 ; x<width ; x++, i++ ) {
					Color clr = new Color(source.image.getRGB(x, y));
					int red = clr.getRed() + (int)(rand.nextGaussian() * dev);
					int green = clr.getGreen() + (int)(rand.nextGaussian() * dev);
					int blue = clr.getBlue() + (int)(rand.nextGaussian() * dev);
					red = red < 0 ? 0 : red > 255 ? 255 : red;
					green = green < 0 ? 0 : green > 255 ? 255 : green;
					blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;
					source.image.setRGB(x, y, (new Color(red, green, blue)).getRGB());
				}
			source.repaint();
		}
		if (((Button)e.getSource()).getLabel().equals("5x5 mean") ){

			BufferedImage img_new = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

			int red, green, blue, rgb;
			int [][] rgb_matrix = new int[width][height];
			int w = 2; // 2w + 1 = 5
			int sum_red = 5;
			int sum_green = 5;
			int sum_blue = 5;

			int [][] F_red = new int[width][height];
			int [][] F_green = new int[width][height];
			int [][] F_blue = new int[width][height];

			int [][] T_red = new int[width][height];
			int [][] T_green = new int[width][height];
			int [][] T_blue = new int[width][height];

			int [][] G_red = new int[width][height];
			int [][] G_green = new int[width][height];
			int [][] G_blue = new int[width][height];

			for (int q=0; q<height; q++){
				for (int p=0; p<width; p++){

					Color clr = new Color(source.image.getRGB(p, q));
					red = clr.getRed();
					green = clr.getGreen();
					blue = clr.getBlue();

					red = red < 0 ? 0 : red > 255 ? 255 : red;
					green = green < 0 ? 0 : green > 255 ? 255 : green;
					blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;

					rgb_matrix[p][q] = (red<<16)|(green<<8)|blue;

					F_red[p][q] = red;
					F_green[p][q] = green;
					F_blue[p][q] = blue;
				}
			}

			for (int q=0; q<height; q++){

				// sum += F[q][u + w]
				// T[q][w] = sum/(2w + 1)

				// red
				sum_red = 3 * F_red[0][q] + F_red[1][q] + F_red[2][q];
				T_red[0][q] = normalise(sum_red/(2*w + 1));
				sum_red = 2 * F_red[0][q] + F_red[1][q] + F_red[2][q] + F_red[3][q];
				T_red[1][q] = normalise(sum_red/(2*w + 1));
				sum_red = F_red[0][q] + F_red[1][q] + F_red[2][q] + F_red[3][q] + F_red[4][q];
				T_red[2][q] = normalise(sum_red/(2*w + 1));

				// green
				sum_green = 3 * F_green[0][q] + F_green[1][q] + F_green[2][q];
				T_green[0][q] = normalise(sum_green/(2*w + 1));
				sum_green = 2 * F_green[0][q] + F_green[1][q] + F_green[2][q] + F_green[3][q];
				T_green[1][q] = normalise(sum_green/(2*w + 1));
				sum_green = F_green[0][q] + F_green[1][q] + F_green[2][q] + F_green[3][q] + F_green[4][q];
				T_green[2][q] = normalise(sum_green/(2*w + 1));

				// blue
				sum_blue = 3 * F_blue[0][q] + F_blue[1][q] + F_blue[2][q];
				T_blue[0][q] = normalise(sum_blue/(2*w + 1));
				sum_blue = 2 * F_blue[0][q] + F_blue[1][q] + F_blue[2][q] + F_blue[3][q];
				T_blue[1][q] = normalise(sum_blue/(2*w + 1));
				sum_blue = F_blue[0][q] + F_blue[1][q] + F_blue[2][q] + F_blue[3][q] + F_blue[4][q];
				T_blue[2][q] = normalise(sum_blue/(2*w + 1));


				for (int p=w+1; p<width-w; p++){

					// sum += F[q][p+w] – F[q][p-w-1],
					// T[q][p] = sum / (2*w+1);

					//red
					sum_red += F_red[p + w][q] - F_red[p - w - 1][q];
					T_red[p][q] = normalise(sum_red/(2*w + 1));
					//green
					sum_green += F_green[p + w][q] - F_green[p - w - 1][q];
					T_green[p][q] = normalise(sum_green/(2*w + 1));
					//blue
					sum_blue += F_blue[p + w][q] - F_blue[p - w - 1][q];
					T_blue[p][q] = normalise(sum_blue/(2*w + 1));
				}
			}

			for (int p=0; p<width; p++){

				// red
				sum_red = 3*T_red[p][0] + T_red[p][1] + T_red[p][2];
				G_red[p][0] = normalise(sum_red/(2*w + 1));
				sum_red = 2*T_red[p][0]+ T_red[p][1] + T_red[p][2] + T_red[p][3];
				G_red[p][1] = normalise(sum_red/(2*w + 1));
				sum_red = T_red[p][0] + T_red[p][1] + T_red[p][2] + T_red[p][3] + T_red[p][4];
				G_red[p][2] = normalise(sum_red/(2*w + 1));

				// green
				sum_green = 3*T_green[p][0] + T_green[p][1] + T_green[p][2];
				G_green[p][0] = normalise(sum_green/(2*w + 1));
				sum_green = 2*T_green[p][0]+ T_green[p][1] + T_green[p][2] + T_green[p][3];
				G_green[p][1] = normalise(sum_green/(2*w + 1));
				sum_green = T_green[p][0] + T_green[p][1] + T_green[p][2] + T_green[p][3] + T_green[p][4];
				G_green[p][2] = normalise(sum_green/(2*w + 1));

				// blue
				sum_blue = 3*T_blue[p][0] + T_blue[p][1] + T_blue[p][2];
				G_blue[p][0] = normalise(sum_blue/(2*w + 1));
				sum_blue = 2*T_blue[p][0]+ T_blue[p][1] + T_blue[p][2] + T_blue[p][3];
				G_blue[p][1] = normalise(sum_blue/(2*w + 1));
				sum_blue = T_blue[p][0] + T_blue[p][1] + T_blue[p][2] + T_blue[p][3] + T_blue[p][4];
				G_blue[p][2] = normalise(sum_blue/(2*w + 1));


				// incremental
				for(int q=w+1; q<height-w; q++){
					sum_red += T_red[p][q+w] - T_red[p][q-w-1];
					G_red[p][q] = normalise(sum_red / (2*w+1));

					sum_green += T_green[p][q+w] - T_green[p][q-w-1];
					G_green[p][q] = normalise(sum_green / (2*w+1));

					sum_blue += T_blue[p][q+w] - T_blue[p][q-w-1];
					G_blue[p][q] = normalise(sum_blue / (2*w+1));

					rgb_matrix[p][width - 1] = G_red[p][0]<<16 | G_green[p][0]<<8 | G_blue[p][0];
					rgb_matrix[p][width - 2] = G_red[p][1]<<16 | G_green[p][1]<<8 | G_blue[p][1];

					rgb_matrix[p][q] = (G_red[p][q])<<16|((G_green[p][q])<<8)|(G_blue[p][q]);
					//rgb = (G_red[p][q])<<16|((G_green[p][q])<<8)|(G_blue[p][q]);
					//img_new.setRGB(p, q, rgb);
				}
			}
			// solve boundaries
			Boundaries_handle(rgb_matrix);

			for ( int q = 0; q < height; q++){
				for ( int p = 0; p < width; p++ ){
					rgb = rgb_matrix[p][q];
					img_new.setRGB(p, q, rgb);
				}
			}
			target.resetImage(img_new);
		}
		else if ( ((Button)e.getSource()).getLabel().equals("5x5 Gaussian") )
		{
			//Initialize
			double sigma = Double.parseDouble(texSigma.getText());
			if(sigma <= 0.0) {
				sigma = 1.0;
			}

			BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			int rad = 5;

			double [][] F_red = new double[width][height];
			double [][] F_green = new double[width][height];
			double [][] F_blue = new double[width][height];

			/*double [][] T_red = new double[width][height];
			double [][] T_green = new double[width][height];
			double [][] T_blue = new double[width][height];
			double [][] G_red = new double[width][height];
			double [][] G_green = new double[width][height];
			double [][] G_blue = new double[width][height];*/


			//Obtain kernel using the input sigma/variance
			double[][] kernel = GetKernel(sigma);

			double[] kx = GetKernel_1D(sigma);
			double[] ky = GetKernel_1D(sigma);

			//Convolute for each channel with x, y values
			for(int x =0; x<width-rad; x++) {
				for(int y=0; y<height-rad; y++) {
					double [][] G_red = new double[rad][rad];
					double [][] G_green = new double[rad][rad];
					double [][] G_blue = new double[rad][rad];

					for(int i=0; i<kernel.length; i++) {
						for(int j=0; j<kernel[i].length; j++) {

							try{
								//p+u
								int newX = x + i - (kernel.length/2);
								//q+v
								int newY = y + j - (kernel.length/2);

								//k[u][v]
								double currentkernel = kernel[i][j];

								Color newClr = new Color(source.image.getRGB(newX, newY));

								//G[p][q] += k[u][v] * F[p+u][q+v]
								G_red[i][j] = currentkernel * newClr.getRed();
								G_green[i][j] = currentkernel * newClr.getGreen();
								G_blue[i][j] = currentkernel * newClr.getBlue();
							}
							catch(Exception ex) {
								//prints message when the placed kernel's edge is out of the image boundary
								//System.out.println("Coordinate out of bound!");
							}
						}
					}

					newImage.setRGB(x, y, (new Color(GetColorValue(G_red), GetColorValue(G_green), GetColorValue(G_blue))).getRGB());
				}
			}

			//Handle boundary values and display image
			int [][] rgb_matrix = GetNewImageMatrix(newImage);
			rgb_matrix = Boundaries_handle_2(rgb_matrix);
			int rgb;
			for ( int q = 0; q < height; q++){
				for ( int p = 0; p < width; p++ ){
					rgb = rgb_matrix[p][q];
					newImage.setRGB(p, q, rgb);
				}
			}

			System.out.print("almost done");
			target.resetImage(newImage);

		}
		if ( ((Button)e.getSource()).getLabel().equals("5x5 median") ){

			BufferedImage img_new = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

			int red, green, blue, rgb;
			int masksize = 5;
			int w = (masksize-1)/2; // 2w+1 = 5
			int red_med, green_med, blue_med;

			int [][] rgb_matrix = new int[width][height];
			int [] F_red_new = new int[masksize * masksize];
			int [] F_green_new = new int[masksize * masksize];
			int [] F_blue_new = new int[masksize * masksize];


			for(int q=0; q<height; q++){
				for (int p=0; p<width; p++){

					Color clr = new Color(input.getRGB(p,q));
					red = clr.getRed();
					green = clr.getGreen();
					blue = clr.getBlue();

					red = red < 0 ? 0 : red > 255 ? 255 : red;
					green = green < 0 ? 0 : green > 255 ? 255 : green;
					blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;

					rgb_matrix[p][q] = (red<<16)|(green<<8)|blue;
				}
			}

			for (int q=w; q<height-w; q++){
				for(int p=w; p<width-w; p++){
					int i=0;
					for (int v=-w; v<=w; v++){
						for(int u=-w; u<=w; u++){
							Color clr = new Color(input.getRGB(p+u,q+v));
							red = clr.getRed();
							green = clr.getGreen();
							blue = clr.getBlue();

							F_red_new[i] = red;
							F_green_new[i] = green;
							F_blue_new[i] = blue;
							i++;
						}
					}

					red_med = getMed(F_red_new);
					green_med = getMed(F_green_new);
					blue_med = getMed(F_blue_new);

					rgb_matrix[p][q] = (red_med<<16)|(green_med<<8)|blue_med;
					//rgb = (red_med)<<16|((green_med)<<8)|(blue_med);
					//img_new.setRGB(p, q, rgb);
				}
			}
			Boundaries_handle(rgb_matrix);
			for ( int q = 0; q < height; q++){
				for ( int p = 0; p < width; p++ ){
					rgb = rgb_matrix[p][q];
					img_new.setRGB(p, q, rgb);
				}
			}
			target.resetImage(img_new);
		}

        else if( ((Button)e.getSource()).getLabel().equals("5x5 Kuwahara") ) {
            BufferedImage img_new = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int[][] originalLuminaceArray = new int[height][width];
            int[][] newLuminaceArray = new int[height][width];
            for ( int y=0; y<height ; y++ ){
				for ( int x=0 ; x<width ; x++) {
                    Color clr = new Color(source.image.getRGB(x, y));
                    float red = (float)clr.getRed()/255;
                    float green = (float)clr.getGreen()/255;
                    float blue = (float)clr.getBlue()/255;

                    float max = Math.max(Math.max(red, green), blue);
                    float min = Math.min(Math.min(red, green), blue);

                    int lumin = Math.round((max + min) / 2 * 100);
                    originalLuminaceArray[x][y] = lumin;
                }
            }

            // Put it in nice and slow
            for ( int y=0; y < height; y++ ){
				for ( int x=0 ; x<width; x++ ) {
                    int[][] luminaceMatrix = new int[5][5];
                    float mean1 = 0;
                    float mean2 = 0;
                    float mean3 = 0;
                    float mean4 = 0;
                    float vari1 = 0;
                    float vari2 = 0;
                    float vari3 = 0;
                    float vari4 = 0;
                    // Get the original luminance
                    for(int col = 0; col < 5; col++){
                        for(int row = 0; row < 5; row++){
                            // What is going into the 5x5 matrix

                            if(x - 2 + col < 0 || x - 2 + col - width >= 0 || y - 2 + row < 0 || y - 2 + row - height >= 0){
                                // Bottom Right Corner
                                if(x - 2 + col - width >= 0 && y - 2 + row - height >= 0){
                                    luminaceMatrix[col][row] = originalLuminaceArray[x - 2 + col - (x - 2 + col - width + 1)][y - 2 + row - (y - 2 + row - height + 1)];
                                }
                                // Top Left Corner
                                else if(x - 2 + col < 0 && y - 2 + row < 0){
                                    luminaceMatrix[col][row] = originalLuminaceArray[x - 2 + col - (x - 2 + col)][y - 2 + row - (y - 2 + row)];
                                }
                                // Bottom Left Corner
                                else if(x - 2 + col < 0 && y - 2 + row - height >= 0){
                                    luminaceMatrix[col][row] = originalLuminaceArray[x - 2 + col - (x - 2 + col)][y - 2 + row - (y - 2 + row - height + 1)];
                                }
                                // Top Right Corner
                                else if((x - 2 + col - width) >= 0 && (y - 2 + row) < 0){
                                    luminaceMatrix[col][row] = originalLuminaceArray[x - 2 + col - (x - 2 + col - width + 1)][y - 2 + row - (y - 2 + row)];
                                }
                                // Top Wall
                                else if((y - 2 + row) < 0 && (x - 2 + col) >= 0 && x - 2 + col - width < 0){
                                    luminaceMatrix[col][row] = originalLuminaceArray[x - 2 + col][y - 2 + row - (y - 2 + row)];
                                }
                                // Bottom Wall
                                else if(y - 2 + row - height >= 0 && x - 2 + col >= 0 && x - 2 + col - width < 0){
                                    luminaceMatrix[col][row] = originalLuminaceArray[x - 2 + col][y - 2 + row - (y - 2 + row - height + 1)]; // Potential Out of Bounds
                                }
                                // Right Wall
                                else if(x - 2 + col < 0 && (y - 2 + row) >= 0 && (y - 2 + row - height) < 0){
                                    luminaceMatrix[col][row] = originalLuminaceArray[x - 2 + col - (x - 2 + col)][y - 2 + row];
                                }
                                // Left Wall
                                else if(x - 2 + col - width >= 0 && y - 2 + row >= 0 && y - 2 + row - height < 0){
                                    luminaceMatrix[col][row] = originalLuminaceArray[x - 2 + col - (x - 2 + col - width + 1)][y - 2 + row];
                                }
                                else{
                                    System.out.println("We made an oopsie");
                                    System.out.println(x-2+col);
                                    System.out.println(y-2+row);
                                }

                            }
                            else{
                                luminaceMatrix[col][row] = originalLuminaceArray[x - 2 + col][y - 2 + row];
                            }
                        }
                    }
                    // Mean of First Quadrant 0-2 top
                    for(int c = 0; c < 3; c++){
                        for(int r = 0; r < 3; r++){
                            mean1 += luminaceMatrix[c][r];
                        }
                    }
                    mean1 /= 9;
                    // Variance of First Quadrant 0-2 top
                    for(int c = 0; c < 3; c++){
                        for(int r = 0; r < 3; r++){
                            vari1 += (luminaceMatrix[c][r] - mean1) * (luminaceMatrix[c][r] - mean1);
                        }
                    }
                    vari1 /= 8;
                    // Mean of Second Quadrant 2-5 top
                    for(int c = 0; c < 3; c++){
                        for(int r = 2; r < 5; r++){
                            mean2 += luminaceMatrix[c][r];
                        }
                    }
                    mean2 /= 9;
                    // Variance of Second Quadrant 2-5 top
                    for(int c = 0; c < 3; c++){
                        for(int r = 2; r < 5; r++){
                            vari2 += (luminaceMatrix[c][r] - mean2) * (luminaceMatrix[c][r] - mean2);
                        }
                    }
                    vari2 /= 8;
                    // Mean of Third Quadrant 0-2 bot
                    for(int c = 2; c < 5; c++){
                        for(int r = 0; r < 3; r++){
                            mean3 += luminaceMatrix[c][r];
                        }
                    }
                    mean3 /= 9;
                    // Variance of Third Quadrant 0-2 bot
                    for(int c = 2; c < 5; c++){
                        for(int r = 0; r < 3; r++){
                            vari3 += (luminaceMatrix[c][r] - mean3) * (luminaceMatrix[c][r] - mean3);
                        }
                    }
                    vari3 /= 8;
                    // Mean of Forth Quadrant 2-5 bot
                    for(int c = 2; c < 5; c++){
                        for(int r = 2; r < 5; r++){
                            mean4 += luminaceMatrix[c][r];
                        }
                    }
                    mean4 /= 9;
                    // Variance of Forth Quadrant 2-5 bot
                    for(int c = 2; c < 5; c++){
                        for(int r = 2; r < 5; r++){
                            vari4 += (luminaceMatrix[c][r] - mean4) * (luminaceMatrix[c][r] - mean4);
                        }
                    }
                    vari4 /= 8;

                    float min = Math.min(Math.min(Math.min(vari1, vari2), vari3), vari4);

                    if(min == vari1){
                        newLuminaceArray[x][y] = Math.round(mean1);
                    }
                    else if(min == vari2){
                        newLuminaceArray[x][y] = Math.round(mean2);
                    }
                    else if(min == vari3){
                        newLuminaceArray[x][y] = Math.round(mean3);
                    }
                    else{
                        newLuminaceArray[x][y] = Math.round(mean4);
                    }
                }
            }

            // Change the color of the pixel
            for(int w = 0; w < width; w++)
            {
                for(int h = 0; h < height; h++)
                {
                    Color clr = new Color(input.getRGB(w,h));
                    int rgb, r, b, g, lumint, satint, hueint;
                    float luminace, saturation, hue, red, green, blue, max, min;

                    red = (float)clr.getRed() / 255;
                    green = (float)clr.getGreen() / 255;
                    blue = (float)clr.getBlue() / 255;

                    max = Math.max(Math.max(red, blue), green);
                    min = Math.min(Math.min(red, blue), green);

                    // Calculating the original Lum
                    luminace = (max + min) / (float)2;

                    // Saturation calculation
                    if(max == min) // means that it is grey scale
                    {
                        saturation = 0;
                    }
                    else
                    {
                        if(luminace < 0.5)
                        {
                            saturation = (max - min) / (min + max);
                        }
                        else
                        {
                            saturation = (max - min) / (2 - max - min);
                        }
                    }

                    satint = Math.round(saturation * 100);

                    // Hue calc
                    if(red == max)
                    {
                        hue = (green - blue) / (max - min);
                    }
                    else if(green == max)
                    {
                        hue = 2 + (blue - red) / (max - min);
                    }
                    else // Blue is max
                    {
                        hue = 4 + (red - green) / (max - min);
                    }

                    hueint = Math.round(hue * 60); // Converting it to angles
                    if(hueint < 0) // We don't like negative angles
                    {
                        hueint += 360;
                    }

                    // Get the new Lum from the normalised table
                    lumint = Math.round(newLuminaceArray[w][h]);

                    // Converts the hue sat and the new lum back to RGB code
                    rgb = hslToRGB(hueint, satint, lumint);

                    img_new.setRGB(w, h, rgb);
                }
            }

            target.resetImage(img_new);
        }
	}
	public static void main(String[] args) {
		new SmoothingFilter(args.length==1 ? args[0] : "baboon.png");
	}

    public int hslToRGB(int hueint, int satint, int lumint)
    {
        int r, g, b, rgb;
        float red, green, blue;
        float luminace, saturation, hue, temp1, temp2, tempR, tempB, tempG;

        hue = (float)hueint / 360;
        saturation = (float)satint / 100;
        luminace = (float)lumint / 100;

        if(satint == 0){
            r = Math.round(luminace * 255);
            g = r;
            b = g;
        }
        else{
            //Temp var Calc
            if(luminace < 0.5){
                temp1 = luminace * ((float)1 + saturation);
            }
            else{
                temp1 = luminace + saturation - luminace * saturation;
            }

            // Temp Var 2 Calc
            temp2 = (float)2 * luminace - temp1;

            // Temp Color Channels Calc
            tempR = hue + (float)0.333;
            tempG = hue;
            tempB = hue - (float)0.333;

            //Check to make sure they are all between 0 and 1
            if(tempR  < 0)
            {
                tempR += 1;
            }
            else if(tempR > 1)
            {
                tempR -= 1;
            }

            if(tempG  < 0)
            {
                tempG += 1;
            }
            else if(tempG > 1)
            {
                tempG -= 1;
            }

            if(tempB  < 0)
            {
                tempB += 1;
            }
            else if(tempB > 1)
            {
                tempB -= 1;
            }

            // Three Tests for Formula required
            // First Red
            if(6 * tempR < 1)
            {
                red = temp2 + (temp1 - temp2) * 6 * tempR;
            }
            else if(2 * tempR < 1)
            {
                red = temp1;
            }
            else if(3 * tempR < 2)
            {
                red = temp2 + (temp1 - temp2) * ((float)0.666 - tempR) * 6;
            }
            else
            {
                red = temp2;
            }
            // First Green
            if(6 * tempG < 1)
            {
                green = temp2 + (temp1 - temp2) * 6 * tempG;
            }
            else if(2 * tempG < 1)
            {
                green = temp1;
            }
            else if(3 * tempG < 2)
            {
                green = temp2 + (temp1 - temp2) * ((float)0.666 - tempG) * 6;
            }
            else{
                green = temp2;
            }
            // First Blue
            if(6 * tempB < 1)
            {
                blue = temp2 + (temp1 - temp2) * 6 * tempB;
            }
            else if(2 * tempB < 1)
            {
                blue = temp1;
            }
            else if(3 * tempB < 2)
            {
                blue = temp2 + (temp1 - temp2) * ((float)0.666 - tempB) * 6;
            }
            else
            {
                blue = temp2;
            }

            //Convert to 8 bit
            r = Math.round(red * 255);
            g = Math.round(green * 255);
            b = Math.round(blue * 255);
        }
        // Put it all together
        rgb = (r&0x0ff)<<16|((g&0x0ff)<<8)|(b&0x0ff);
        return rgb;
    }

	public static int normalise(int color_channel){
		if (color_channel > 255){
			color_channel = 255;
		}
		if (color_channel < 0){
			color_channel = 0;
		}
		return color_channel;
	}
	public void Boundaries_handle(int[][] matrix){
		int clr = 240;
		for (int q = 0; q<height; q++){
			matrix[height - 1][q] = (clr<<16) | (clr<<8) | clr;
			matrix[height - 2][q] = (clr<<16) | (clr<<8) | clr;
			matrix[q][height - 1] = (clr<<16) | (clr<<8) | clr;
			matrix[q][height - 2] = (clr<<16) | (clr<<8) | clr;
			matrix[0][q] = (clr<<16) | (clr<<8) | clr;
			matrix[1][q] = (clr<<16) | (clr<<8) | clr;
			matrix[q][0] = (clr<<16) | (clr<<8) | clr;
			matrix[q][1] = (clr<<16) | (clr<<8) | clr;
		}
	}
	// get median calculation
	public int getMed(int[] F_color){
		int med;
		int length = F_color.length;
		java.util.Arrays.sort(F_color);
		if (length % 2 == 0){
			med = (int)(F_color[length/2] + F_color[length/2 - 1])/2;
		}
		else{
			med = (int)F_color[length/2];
		}
		return med;
	}

	public double[][] GetKernel(double sigma) {
		//initialize 5x5 kernel
		int rad = 5;
		double[][] kernel = new double[rad][rad];
		double sum = 0.0;
		int i, j;
		//generating a 5x5 kernel
		for(i=0; i<kernel.length; i++) {
			for(j=0; j<kernel[i].length; j++) {
				//since the cenre (0,0) is at left top, make the middle of the image centre by (-rad/2)
				kernel[i][j] = GaussianFunction_2D(i - rad/2, j - rad/2, sigma);
				sum += kernel[i][j];
			}
		}
		//normalizing the 5x5 kernel
		for (i=0 ; i<kernel.length ; i++) {
			for (j=0 ; j<kernel[i].length ; j++) {
				kernel[i][j] /= sum;
			}
		}

		return kernel;
	}
	public int[][] GetNewImageMatrix(BufferedImage image) {
		int red, green, blue, rgb;
		int [][] rgb_matrix = new int[width][height];
		for (int q=0; q<height; q++){
			for (int p=0; p<width; p++){

				Color clr = new Color(image.getRGB(p, q));
				red = clr.getRed();
				green = clr.getGreen();
				blue = clr.getBlue();

				red = red < 0 ? 0 : red > 255 ? 255 : red;
				green = green < 0 ? 0 : green > 255 ? 255 : green;
				blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;

				rgb_matrix[p][q] = (red<<16)|(green<<8)|blue;

			}
		}
		return rgb_matrix;
	}

	public double[] GetKernel_1D(double sigma) {
		//initialize 5x5 kernel
		int rad = 5;
		double[] kernel = new double[rad];
		double sum = 0.0;
		int i;
		//generating a 5x5 kernel
		for(i=0; i<kernel.length; i++) {
				//since the cenre (0,0) is at left top, make the middle of the image centre by (-rad/2)
				kernel[i] = GaussianFunction_1D(i - rad/2, sigma);
				sum += kernel[i];
		}
		//normalizing the 5x5 kernel
		for (i=0 ; i<kernel.length ; i++) {
				kernel[i] /= sum;
		}

		return kernel;
	}


	public int GetColorValue(double[][] c) {
		double sum = 0.0;
		for(int i=0; i<c.length; i++) {
			for(int j=0; j<c[i].length; j++) {
				sum += c[i][j];
			}
		}
		return (int)sum;
	}

	public double GaussianFunction_2D(double x, double y, double sigma) {
		return (1/(2* Math.PI * Math.pow(sigma, 2)))* Math.exp(-(Math.pow(x, 2) + Math.pow(y, 2))/(2 * Math.pow(sigma, 2)));
	}
	public double GaussianFunction_1D(double x, double sigma) {
		return (1/(2* Math.PI * sigma))* Math.exp(-(Math.pow(x, 2))/(2 * Math.pow(sigma, 2)));
	}

	public int[][] Boundaries_handle_2(int[][] matrix) {
		System.out.println("width: " +  source.image.getWidth());
		System.out.println("height: " +  height);
		System.out.println("matrix width: " + matrix.length);
		System.out.println("matrix height: " +  matrix[0].length);
		//For the pixels near the boundary, make it the same as the neighbouring pixel
		for (int x = 250; x<width; x++) {
			for(int y = 0; y<height; y++) {
				matrix[x][y] = matrix[x-1][y];
			}
		}
		for (int y = 250; y<height; y++) {
			for(int x = 0; x<width; x++) {
				matrix[x][y] = matrix[x][y-1];
			}
		}
		return matrix;
	}
}
