// Skeletal program for the "Image Threshold" assignment

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.*;

// Main class
public class ImageThreshold extends Frame implements ActionListener {
	BufferedImage input;
	int width, height;
	TextField texThres, texOffset;
	ImageCanvas source, target;
	PlotCanvas2 plot;
	// Constructor
	public ImageThreshold(String name) {
		super("Image Histogram");
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
		plot = new PlotCanvas2(256, 200);
		target = new ImageCanvas(width, height);
		//target.copyImage(input);
        target.resetImage(input);
		main.setLayout(new GridLayout(1, 3, 10, 10));
		main.add(source);
		main.add(plot);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		controls.add(new Label("Threshold:"));
		texThres = new TextField("128", 2);
		controls.add(texThres);
		Button button = new Button("Manual Selection");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Automatic Selection");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Otsu's Method");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Offset:"));
		texOffset = new TextField("10", 2);
		controls.add(texOffset);
		button = new Button("Adaptive Mean-C");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+400, height+100);
		setVisible(true);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {
		// example -- compute the average color for the image
		if ( ((Button)e.getSource()).getLabel().equals("Manual Selection") ) {
			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			/* int threshold;
			//int[] histogram = imageHistogram(img2);
			try {
				threshold = Integer.parseInt(texThres.getText());
			} catch (Exception ex) {
				texThres.setText("128");
				threshold = 128;
			} */
			int threshold = Integer.parseInt(texThres.getText());
			int[][] imageMatrix = new int[height][width];
			int[][] red = new int[height][width];
			int[][] green = new int[height][width];
			int[][] blue = new int[height][width];

			for(int y=0; y<height; y++){
				for(int x=0; x<width; x++){
					Color clr = new Color(source.image.getRGB(y, x));
					if (clr.getRed() < threshold){
						red[y][x] = 0;
					}
					if (clr.getRed() >= threshold){
						red[y][x] = 255;
					}
					if (clr.getGreen() < threshold){
						green[y][x] = 0;
					}
					if (clr.getGreen() >= threshold){
						green[y][x] = 255;
					}
					if (clr.getBlue() < threshold){
						blue[y][x] = 0;
					}
					if (clr.getBlue() >= threshold){
						blue[y][x] = 255;
					}
					imageMatrix[y][x] = red[y][x]<<16 | green[y][x]<<8 | blue[y][x];
				}
				//source.repaint();
			}
			// draw threshold value line
			plot.clearObjects();
			plot.addObject(new VerticalBar(Color.BLACK, threshold, 200));

			int[] histogramR = new int[256];
		    int[] histogramG = new int[256];
		    int[] histogramB = new int[256];

	        for(int i=0; i<input.getWidth(); i++) {
	            for(int j=0; j<input.getHeight(); j++) {
					Color clr = new Color(source.image.getRGB(i, j));
					int red1 = clr.getRed();
					int green1 = clr.getGreen();
					int blue1 = clr.getBlue();
	                histogramR[red1]++;
	                histogramG[green1]++;
	                histogramB[blue1]++;
	            }
			}
			//System.out.println(width);
			for (int k=0; k<255; k++){
				plot.addObject(new VerticalBar(Color.RED, k, histogramR[k]/8));
	    		plot.addObject(new VerticalBar(Color.GREEN, k, histogramG[k]/8));
				plot.addObject(new VerticalBar(Color.BLUE, k, histogramB[k]/8));
			}
			// get output image
			for (int y=0; y<height; y++){
				for (int x=0; x<width; x++){
					img.setRGB(y, x, imageMatrix[y][x]);
				}
			}
			target.resetImage(img);
		}
		if ( ((Button)e.getSource()).getLabel().equals("Automatic Selection") ) {
			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			int[][] ImageMatrix = new int[height][width];
			int[][] red = new int[height][width];
			int[][] green = new int[height][width];
			int[][] blue = new int[height][width];
			int threshold_Red = 128;
			int threshold_Green = 128;
			int threshold_Blue = 128;
			threshold_Red = select_auto(threshold_Red, "red");
			threshold_Green = select_auto(threshold_Green, "green");
			threshold_Blue = select_auto(threshold_Blue, "blue");

			// apply threshold value to the image
			for (int y=0; y<height; y++){
				for(int x=0; x<width; x++){
					Color clr = new Color(source.image.getRGB(y, x));
					if (clr.getRed() < threshold_Red){
						red[y][x] = 0; // set black
					}
					if (clr.getRed() >= threshold_Red){
						red[y][x] = 255; // set white
					}
					if (clr.getGreen() < threshold_Green){
						green[y][x] = 0; // set black
					}
					if (clr.getGreen() >= threshold_Green){
						green[y][x] = 255; // set white
					}
					if (clr.getBlue() < threshold_Blue){
						blue[y][x] = 0; // set black
					}
					if (clr.getBlue() >= threshold_Blue){
						blue[y][x] = 255; // set white
					}
					ImageMatrix[y][x] = red[y][x] << 16 | green[y][x] << 8 | blue[y][x];
				}
			}
			// draw the threshold value line
			plot.clearObjects();
			plot.addObject(new VerticalBar(Color.RED, threshold_Red, 200));
			plot.addObject(new VerticalBar(Color.GREEN, threshold_Green, 200));
			plot.addObject(new VerticalBar(Color.BLUE, threshold_Blue, 200));

			// hist for input img
			int[] histogramR = new int[256];
		    int[] histogramG = new int[256];
		    int[] histogramB = new int[256];

	        for(int i=0; i<input.getWidth(); i++) {
	            for(int j=0; j<input.getHeight(); j++) {
					Color clr = new Color(source.image.getRGB(i, j));
					int red1 = clr.getRed();
					int green1 = clr.getGreen();
					int blue1 = clr.getBlue();
	                histogramR[red1]++;
	                histogramG[green1]++;
	                histogramB[blue1]++;
	            }
			}
			//System.out.println(width);
			for (int k=0; k<255; k++){
				plot.addObject(new VerticalBar(Color.RED, k, histogramR[k]/8));
	    		plot.addObject(new VerticalBar(Color.GREEN, k, histogramG[k]/8));
				plot.addObject(new VerticalBar(Color.BLUE, k, histogramB[k]/8));
			}


			for (int y=0; y<height; y++){
				for(int x=0; x<width; x++){
					img.setRGB(y, x, ImageMatrix[y][x]);
				}
			}
			target.resetImage(img);
		}
		if (((Button)e.getSource()).getLabel().equals("Adaptive Mean-C")){
            	    BufferedImage new_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

                    int[][] reds = new int[width][height];
                    int[][] greens = new int[width][height];
            	    int[][] blues = new int[width][height];
            	    int[][] freshInt = new int[width][height];

            	    int[] redInt = new int[256];
            	    int[] greenInt = new int[256];
            	    int[] blueInt = new int[256];

            	    String value = texOffset.getText();
            	    int constantC = Integer.valueOf(value);

            	    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                    	    Color clr = new Color(source.image.getRGB(x, y));

                    	    reds[x][y] = clr.getRed();
                    	    greens[x][y] = clr.getGreen();
                    	    blues[x][y] = clr.getBlue();

                    	    redInt[clr.getRed()]++;
                    	    greenInt[clr.getGreen()]++;
                    	    blueInt[clr.getBlue()]++;
                        }
            	    }

            	    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                    	    Color clr = new Color(source.image.getRGB(x,y));

                    	    float meanR = 0;
                    	    float meanG = 0;
                    	    float meanB = 0;

                    	    int ogR = clr.getRed();
                    	    int ogG = clr.getGreen();
                    	    int ogB = clr.getBlue();

                    	    int[][] kernalR = new int[7][7];
                    	    int[][] kernalG = new int[7][7];
                    	    int[][] kernalB = new int[7][7];

                            for (int col = 0; col < 7; col++){
                                for (int row = 0; row < 7; row++){
                                    int colP = x - 3 + col;
                                    int rowP = y - 3 + row;

                                    if(colP < 0){
                                        colP = 0;
                                    }
                                    else if(colP >= width){
                                        colP = width-1;
                                    }

                                    if(rowP < 0){
                                        rowP = 0;
                                    }
                                    else if(rowP >= height){
                                        rowP = height-1;
                                    }

                                    kernalR[col][row] = reds[colP][rowP];
                                    kernalG[col][row] = greens[colP][rowP];
                                    kernalB[col][row] = blues[colP][rowP];

                                    // Mean Adding
                                    meanR += reds[colP][rowP];
                                    meanG += greens[colP][rowP];
                                    meanB += blues[colP][rowP];
                                }
                            }
                            // Divide by 49 for
                            meanR /= (float)49;
                            meanR -= constantC;
                            meanG /= (float)49;
                            meanG -= constantC;
                            meanB /= (float)49;
                            meanB -= constantC;
                            // Red
                    	    if(ogR < meanR){
                                ogR = 0;
                    	    }
                    	    else{
                                ogR = 255;
                    	    }
                    	    // Green
                    	    if(ogG < meanG){
                                ogG = 0;
                    	    }
                    	    else{
                                ogG = 255;
                    	    }
                    	    // Blue
                    	    if(ogB < meanB){
                                ogB = 0;
                    	    }
                    	    else{
                                ogB = 255;
                    	    }
                    new_img.setRGB(x, y, ogR << 16 | ogG << 8 | ogB);
                    }
                }
		plot.clearObjects();
            for(int x = 0; x < 256; x++){
                plot.addObject(new VerticalBar(Color.RED, x, redInt[x]/20));
                plot.addObject(new VerticalBar(Color.GREEN, x, greenInt[x]/20));
                plot.addObject(new VerticalBar(Color.BLUE, x, blueInt[x]/20));
            }

            target.resetImage(new_img);
        }
		if ( ((Button)e.getSource()).getLabel().equals("Otsu's Method") ) {
			//initialize
			int gray_scale = 256;
			int[] grey_hist = new int[gray_scale];
			int[] red_hist = new int[gray_scale];
			int[] green_hist = new int[gray_scale];
			int[] blue_hist = new int[gray_scale];

			BufferedImage img_new = new BufferedImage(width, height, input.getType());

			ArrayList<Float> variance_List = new ArrayList<Float>();
			ArrayList<Integer> threshold_List = new ArrayList<Integer>();

			//get rgb for input image and convert rgb image to gray scale
			for (int q=0; q<height; q++)
            {
				for (int p=0; p<width; p++)
                {
					// get the x and y coordinates of RGB from input image
					Color clr = new Color(input.getRGB(p,q));
					red_hist[clr.getRed()]++;
					green_hist[clr.getGreen()]++;
					blue_hist[clr.getBlue()]++;
					// convert rgb image to gray scale
					int rgb = input.getRGB(p, q);

					int r = (rgb >> 16) & 0xFF;
					int g = (rgb >> 8) & 0xFF;
					int b = (rgb & 0xFF);

					int gray= input.getRGB(p, q)& 0xFF;
					grey_hist[gray]++;
				}
			}

			//get threshold values for each color channels
			int red_threshold = GetOtsuThreshold(red_hist);
			int green_threshold = GetOtsuThreshold(green_hist);
			int blue_threshold = GetOtsuThreshold(blue_hist);
			System.out.println(red_threshold);
			System.out.println(green_threshold);
			System.out.println(blue_threshold);

			int r, g, b, value;
			int vr =0;
			int vg = 0;
			int vb = 0;
			for(int p=0; p<width; p++) {
				for(int q=0; q<height; q++) {
					//
					Color clr = new Color(input.getRGB(p,q));
					//get pixel values using the threshold
					r = clr.getRed();
					g = clr.getGreen();
					b = clr.getBlue();
					int alpha = new Color(input.getRGB(p, q)).getAlpha();
					if(r > red_threshold) {
						vr = 255;
					}
                    else{
                        vr = 0;
                    }
					if(g > green_threshold) {
						vg = 255;
					}
                    else{
                        vg = 0;
                    }
					if(b > blue_threshold) {
						vb = 255;
					}
					else {
						vb = 0;
					}
					value = GetRGB(alpha, vr, vg, vb);
					img_new.setRGB(p, q, value);
                    // This was changed
				}
			}
            // Add more comments

			//normalize histogram
			/*for (int k=0; k<gray_scale; k++){
				grey_hist[k] = grey_hist[k]/6;
			}
			//plot pixels to histogram
			plot.showSegmtColor(grey_hist);*/

			//show threshold on graph
			plot.clearObjects();
			for (int k=0; k<255; k++){
				plot.addObject(new VerticalBar(Color.RED, k, red_hist[k]/20));
	    			plot.addObject(new VerticalBar(Color.GREEN, k, green_hist[k]/20));
				plot.addObject(new VerticalBar(Color.BLUE, k, blue_hist[k]/20));
			}
			plot.addObject(new VerticalBar(Color.RED, red_threshold, 200));
			plot.addObject(new VerticalBar(Color.GREEN, green_threshold, 200));
			plot.addObject(new VerticalBar(Color.BLUE, blue_threshold, 200));

			target.resetImage(img_new);
		}
	}
	public int GetOtsuThreshold(int[] hist) {
		double vars_max = 0;
		int threshold = 0;
		for (int i=0; i<256; i++){
			double w0 = 0;
			double w1 = 0;
			double u0 = 0;
			double u1 = 0;
			double vars = 0;
			double u = 0;
			double u0_t = 0;
			double u1_t = 0;
			for(int y=0; y<256; y++){
				if (y < i){
					w0 += hist[y];
					u0_t += y*hist[y];
				}
				if (y >= i){
					w1 += hist[y];
					u1_t += y*hist[y];
				}
			}
			u0 = u0_t/w0;
			u1 = u1_t/w1;
			u = u0_t + u1_t;
			// calculate intra-variance
			vars = (w0*Math.pow(u0-u,2)+(w1*Math.pow(u1-u,2)));
			// get the minimum vars
			if (vars > vars_max){
				vars_max = vars;
				threshold = i;
			}
		}
		return threshold;
	}
	public int select_auto(int threshold, String color_name){
		int temp_t = 0;
		int sum_t1 = 0;
		int sum_t2 = 0;
		double avg_u1 = 0;
		double avg_u2 = 0;
		int count1 = 0;
		int count2 = 0;
		while (Math.abs(threshold - temp_t) > 0.01){
			temp_t = threshold;
			for (int y=0; y<height; y++){
				for (int x=0; x<width; x++){
					Color clr = new Color(source.image.getRGB(y, x));
					if(color_name == "red"){
						if (clr.getRed() < threshold){
							sum_t1 += clr.getRed();
							count1++;
						}
						if (clr.getRed() >= threshold){
							sum_t2 += clr.getRed();
							count2++;
						}
					}
					if (color_name == "green"){
						if (clr.getGreen() < threshold){
							sum_t1 += clr.getGreen();
							count1++;
						}
						if (clr.getGreen() >= threshold){
							sum_t2 += clr.getGreen();
							count2++;
						}
					}
					if (color_name == "blue"){
						if (clr.getBlue() < threshold){
							sum_t1 += clr.getBlue();
							count1++;
						}
						if (clr.getBlue() >= threshold){
							sum_t2 += clr.getBlue();
							count2++;
						}
					}
				}
			}
			avg_u1 = (count1 > 0) ? (int)(sum_t1/count1):0;
			avg_u2 = (count2 > 0) ? (int)(sum_t2/count2):0;

			threshold = (int)(avg_u1 + avg_u2)/2;
			System.out.println("threshold: "+ threshold);
		}
		return threshold;
	}
	 public int GetRGB(int alpha, int red, int green, int blue) {

        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red; newPixel = newPixel << 8;
        newPixel += green; newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;
	}
	public static void main(String[] args) {
		new ImageThreshold(args.length==1 ? args[0] : "lena.png");
	}
}
