/*
    Group 19
    Assignment1 Comp3301
    Nicholas Atkins 201509098
    Meheroon Nesa Tondra 201555661
    Yusong Du 201458700
*/

import java.awt.*;
import java.awt.Color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

// Main class
public class ImageHistogram extends Frame implements ActionListener
{
	BufferedImage input;
	int width, height;
	int gray_scale = 256;
	TextField texRad, texThres;
	ImageCanvas source, target;
	PlotCanvas plot;
	// Constructor
	public ImageHistogram(String name)
    {
		super("Image Histogram");
		// load image
		try
        {
			input = ImageIO.read(new File(name));
		}
		catch ( Exception ex )
        {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		plot = new PlotCanvas();
		target = new ImageCanvas(input);
		main.setLayout(new GridLayout(1, 3, 10, 10));
		main.add(source);
		main.add(plot);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Display Histogram");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Histogram Stretch");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Cutoff fraction:"));
		texThres = new TextField("10", 2);
		controls.add(texThres);
		button = new Button("Aggressive Stretch");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Histogram Equalization");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+400, height+100);
		setVisible(true);
	}
	class ExitListener extends WindowAdapter
    {
		public void windowClosing(WindowEvent e)
        {
			System.exit(0);
		}
	}
	// Action listener for button click events
	public void actionPerformed(ActionEvent e)
    {
		if ( ((Button)e.getSource()).getLabel().equals("Display Histogram") )
        {
			// delcared number of pixels
			int[] red_NumPixls = new int[gray_scale];
			int[] green_NumPixls = new int[gray_scale];
			int[] blue_NumPixls = new int[gray_scale];

			for (int q=0; q<height; q++)
            {
				for (int p=0; p<width; p++)
                {
					// get the x and y coordinates of RGB from input image
					Color clr = new Color(input.getRGB(p,q));
					// save all the pixels to array
					red_NumPixls[clr.getRed()]++;
					green_NumPixls[clr.getGreen()]++;
					blue_NumPixls[clr.getBlue()]++;
				}
			}

			// delcared probability of pixels
			double[] red_prob = new double[gray_scale];
			double[] green_prob = new double[gray_scale];
			double[] blue_prob = new double[gray_scale];

			// normalize the pixels
			for (int k=0; k<gray_scale; k++){
				//red_prob[k] = (double)red_NumPixls[k]/(width * height);
				red_NumPixls[k] = red_NumPixls[k]/6;
				//green_prob[k] = (double)green_NumPixls[k]/(width * height);
				green_NumPixls[k] = green_NumPixls[k]/6;
				//blue_prob[k] = (double)blue_NumPixls[k]/(width * height);
				blue_NumPixls[k] = blue_NumPixls[k]/6;
			}

			// plot pixels to histogram
			plot.showSegmtColor(red_NumPixls, green_NumPixls, blue_NumPixls);


            // example -- compute the average color for the image

			/* float red=0, green=0, blue=0;
			for ( int y=0, i=0 ; y<height ; y++ )
				for ( int x=0 ; x<width ; x++, i++ ) {
					Color clr = new Color(input.getRGB(x, y));
					red += clr.getRed();
					green += clr.getGreen();
					blue += clr.getBlue();
				}
			red /= width * height;
			green /= width * height;
			blue /= width * height;
			plot.setMeanColor(new Color((int)red,(int)green,(int)blue)); */
		}
        if ( ((Button)e.getSource()).getLabel().equals("Histogram Stretch") )
        {
            int min = 0;
            int max = gray_scale-1;

            int[] rednew_NumPx = new int[gray_scale];
            int[] greennew_NumPx = new int[gray_scale];
			int[] bluenew_NumPx = new int[gray_scale];

            for(int p=0; p<width; p++)
            {
             	for(int q=0; q<height; q++)
                {
                    Color clr = new Color(input.getRGB(p,q));
                    rednew_NumPx[clr.getRed()]++;
                    greennew_NumPx[clr.getGreen()]++;
                    bluenew_NumPx[clr.getBlue()]++;
             	}
            }

            // implementing stretch
            int red,green,blue,rgb;

            int[] red_after = new int[256];
            int[] green_after = new int[256];
            int[] blue_after = new int[256];

            while(rednew_NumPx[min] == 0)
            {
                min++;
            }
            while (rednew_NumPx[max] == 0)
            {
                max--;
            }
            while(greennew_NumPx[min] == 0)
            {
                min++;
            }
            while (greennew_NumPx[max] == 0)
            {
                max--;
            }
            while(bluenew_NumPx[min] == 0)
            {
                min++;
            }
            while (bluenew_NumPx[max] == 0)
            {
                max--;
            }
            // define new target image
            BufferedImage img_new = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for(int p=0; p<width; p++)
            {
                for(int q=0; q<height; q++)
                {
                    Color clr = new Color(input.getRGB(p,q));

                    red = clr.getRed();
                    green = clr.getGreen();
                    blue = clr.getBlue();

                    red = (red - min) * 255/(max - min);
                    green = (green - min) * 255/(max - min);
                    blue = (blue - min) * 255/(max - min);

                    // make sure rgb is in [0, 255]
                    if(red> 255)
                    {
                        red=255;
                    }
                    if(green> 255)
                    {
                        green=255;
                    }
                    if(blue> 255)
                    {
                        blue=255;
                    }
                    if(red<0)
                    {
                        red=0;
                    }
                    if(green<0)
                    {
                        green=0;
                    }
                    if(blue<0)
                    {
                        blue=0;
                    }

                    red_after[red]++;
                    green_after[green]++;
                    blue_after[blue]++;

                    rgb = (red&0x0ff)<<16|((green&0x0ff)<<8)|(blue&0x0ff);
                    img_new.setRGB(p, q, rgb);
                }
            }

            // normalize
            red_after= normalize(red_after, 0, 255);
            green_after= normalize(green_after, 0, 255);
            blue_after= normalize(blue_after, 0, 255);

            for (int k=0; k<256;k++)
            {
                red_after[k]= red_after[k]/6;
                green_after[k]= green_after[k]/6;
                blue_after[k]= blue_after[k]/6;
            }
            plot.showSegmtColor(red_after, green_after, blue_after);
            // apply to the target image
            target.resetImage(img_new);
        }
        else if ( ((Button)e.getSource()).getLabel().equals("Aggressive Stretch") )
        {
            int cutoff_fraction = Integer.parseInt(texThres.getText());

            if (cutoff_fraction < 0){
                cutoff_fraction = 0;
            }

            int red_min = cutoff_fraction * (gray_scale-1)/100;
            int red_max = (gray_scale-1) - red_min;
            int green_min = cutoff_fraction * (gray_scale-1)/100;
            int green_max = (gray_scale-1) - green_min;
            int blue_min = cutoff_fraction * (gray_scale-1)/100;
            int blue_max = (gray_scale-1) - blue_min;

            BufferedImage img_new = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            
            //Initialize arrays
            int[] red_NumPixls = new int[gray_scale];
            int[] green_NumPixls = new int[gray_scale];
            int[] blue_NumPixls = new int[gray_scale];

            int[] hist_red_after = new int[gray_scale];
            int[] hist_green_after = new int[gray_scale];
            int[] hist_blue_after = new int[gray_scale];

            for(int p=0; p<width; p++)
            {
                for (int q=0; q<height; q++)
                {
                    // get the x and y coordinates of RGB from input image
                    Color clr = new Color(input.getRGB(p,q));
                    // save all the pixels to array
                    red_NumPixls[clr.getRed()]++;
                    green_NumPixls[clr.getGreen()]++;
                    blue_NumPixls[clr.getBlue()]++;
                }
            }

            // Linear stretch for 3 channels
           
            while(red_NumPixls[red_min] == 0)
            {
                red_min++;
            }
            while (red_NumPixls[red_max] == 0)
            {
                red_max--;
            }
            while(green_NumPixls[green_min] == 0)
            {
                green_min++;
            }
            while (green_NumPixls[green_max] == 0)
            {
                green_max--;
            }
            while(blue_NumPixls[blue_min] == 0)
            {
                blue_min++;
            }
            while (blue_NumPixls[blue_max] == 0)
            {
                blue_max--;
            }
            
            int red, blue, green, rgb;
            
            for(int p=0; p<width; p++)
            {
                for (int q=0; q<height; q++)
                {
                    // get the x and y coordinates of RGB from input image
                    Color clr = new Color(input.getRGB(p,q));
                    //F([p][q]) for each channels
                    red = clr.getRed();
                    green = clr.getGreen();
                    blue = clr.getBlue();

                    red = (red - red_min)*gray_scale/(red_max - red_min);
                    green= (green - green_min)*gray_scale/(green_max - green_min);
                    blue= (blue - blue_min)*gray_scale/(blue_max - blue_min);

                    // make sure rgb is in [0, 255]
                    if(red> 255)
                    {
                        red=255;
                    }
                    if(green> 255)
                    {
                        green=255;
                    }
                    if(blue> 255)
                    {
                        blue=255;
                    }
                    if(red<0)
                    {
                        red=0;
                    }
                    if(green<0)
                    {
                        green=0;
                    }
                    if(blue<0)
                    {
                        blue=0;
                    }

                    hist_red_after[red]++;
                    hist_green_after[green]++;
                    hist_blue_after[blue]++;

                    rgb = (red&0x0ff)<<16|((green&0x0ff)<<8)|(blue&0x0ff);
                    img_new.setRGB(p, q, rgb);
                }
            }

            //Normalize
            hist_red_after = normalize(hist_red_after, 0, 255);
            hist_green_after= normalize(hist_green_after, 0, 255);
            hist_blue_after = normalize(hist_blue_after, 0, 255);
            for (int k=0; k<256;k++)
            {
                hist_red_after[k] = hist_red_after[k]/6;
                hist_green_after[k]= hist_green_after[k]/6;
                hist_blue_after[k] = hist_blue_after[k]/6;
            }

            plot.showSegmtColor(hist_red_after, hist_green_after, hist_blue_after);
            target.resetImage(img_new);
        }
        else if (((Button)e.getSource()).getLabel().equals("Histogram Equalization") )
        {
            BufferedImage img_new = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int lightness[] = new int[101];
            int[] hist_red_after = new int[gray_scale];
            int[] hist_green_after = new int[gray_scale];
            int[] hist_blue_after = new int[gray_scale];

            for(int w = 0; w < width; w++)
            {
                for(int h = 0; h < height; h++)
                {
                    int lumint;
                    float red, green, blue, luminace;
                    Color clr = new Color(input.getRGB(w,h));

                    red = (float)clr.getRed() / 255;
                    green = (float)clr.getGreen() / 255;
                    blue = (float)clr.getBlue() / 255;

                    float max = Math.max(Math.max(red, blue), green);
                    float min = Math.min(Math.min(red, blue), green);

                    luminace = (max + min) / (float)2;
                    lumint = Math.round(luminace * 100);

                    lightness[lumint]++;
                }
            }

            // CDF on lightness
            float[] cdf = new float[101];

            cdf[0] = (float)lightness[0] / (height * width);
            for(int i = 1; i < lightness.length; i++)
            {
                cdf[i] = cdf[i-1] + (float)lightness[i] / (height * width);
            }

            // Change the Luminace and convert it back
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
                    lumint = Math.round(luminace * 100);

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
                    luminace = cdf[lumint] * 100;
                    lumint = Math.round(luminace); // Making sure that it is rounded

                    // Converts the hue sat and the new lum back to RGB code
                    rgb = hslToRGB(hueint, satint, lumint);

                    Color newclr = new Color(rgb);

                    hist_red_after[newclr.getRed()]++;
                    hist_green_after[newclr.getGreen()]++;
                    hist_blue_after[newclr.getBlue()]++;


                    img_new.setRGB(w, h, rgb);
                }
            }

            hist_red_after = normalize(hist_red_after, 0, 255);
            hist_green_after= normalize(hist_green_after, 0, 255);
            hist_blue_after = normalize(hist_blue_after, 0, 255);

            for(int i = 0; i < 256; i++)
            {
                hist_red_after[i] /= 6;
                hist_green_after[i] /= 6;
                hist_blue_after[i] /= 6;
            }

            plot.showSegmtColor(hist_red_after, hist_green_after, hist_blue_after);
            target.resetImage(img_new);
        }
    }
	public static void main(String[] args)
    {
		new ImageHistogram(args.length==1 ? args[0] : "baboon.png");
	}

	public int[] normalize(int[] arr, int min, int max)
    {
		int min2 = 0;
		int max2 = 255;
		int range = 256;
		int[] normalized_array = new int[range];

		for (int i=0; i<range; i++) {
			if (i<=min)
				normalized_array[i] = min2;
			else if (i>=max)
				normalized_array[i] = max2;
			else
				normalized_array[i] = (arr[i]-min)*((max2-min2)/(max-min))+min2;
		}
		return normalized_array;
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
}

// Canvas for plotting histogram
class PlotCanvas extends Canvas
{
	// lines for plotting axes and mean color locations
	LineSegment x_axis, y_axis;
	LineSegment red, green, blue;

	// boolean showMean = false;
	boolean showSegmt = false;

	// declared rgb segment
	LineSegment[] redSegment = new LineSegment[255];
	LineSegment[] greenSegment = new LineSegment[255];
	LineSegment[] blueSegment = new LineSegment[255];



	public PlotCanvas()
    {
		x_axis = new LineSegment(Color.BLACK, -10, 0, 256+10, 0);
		y_axis = new LineSegment(Color.BLACK, 0, -10, 0, 200+10);
	}

	// set rgb segment color to plot
	public void showSegmtColor(int[] rp, int[] gp, int[] bp)
    {
		for (int j=0; j<255; j++)
        {

			// index of x position
			// x0 = j
			// x1 = j+1

			// y coordinates as pixels
			// y0 = rp[j]
			// y1 = rp[j+1]
			redSegment[j] = new LineSegment(Color.RED, j, rp[j], j+1, rp[j+1]);
			greenSegment[j] = new LineSegment(Color.GREEN, j, gp[j], j+1, gp[j+1]);
			blueSegment[j] = new LineSegment(Color.BLUE, j, bp[j], j+1, bp[j+1]);
			// draw pixels at each position
			showSegmt = true;
		}
		repaint();
	}

	// set mean image color for plot
	//public void setMeanColor(Color clr) {
	//	red = new LineSegment(Color.RED, clr.getRed(), 0, clr.getRed(), 100);
	//	green = new LineSegment(Color.GREEN, clr.getGreen(), 0, clr.getGreen(), 100);
	//	blue = new LineSegment(Color.BLUE, clr.getBlue(), 0, clr.getBlue(), 100);
	//	showMean = true;
	//	repaint();
	//}

	// redraw the canvas
	public void paint(Graphics g)
    {
		// draw axis
		int xoffset = (getWidth() - 256) / 2;
		int yoffset = (getHeight() - 200) / 2;
		x_axis.draw(g, xoffset, yoffset, getHeight());
		y_axis.draw(g, xoffset, yoffset, getHeight());
		if ( showSegmt )
        {
			//red.draw(g, xoffset, yoffset, getHeight());
			//green.draw(g, xoffset, yoffset, getHeight());
			//blue.draw(g, xoffset, yoffset, getHeight());
			for (int l=0; l<255; l++)
            {
				redSegment[l].draw(g, xoffset, yoffset, getHeight());
				greenSegment[l].draw(g, xoffset, yoffset, getHeight());
				blueSegment[l].draw(g, xoffset, yoffset, getHeight());
			}
		}
	}
}

// LineSegment class defines line segments to be plotted
class LineSegment
{
	// location and color of the line segment
	int x0, y0, x1, y1;
	Color color;
	// Constructor
	public LineSegment(Color clr, int x0, int y0, int x1, int y1)
    {
		color = clr;
		this.x0 = x0; this.x1 = x1;
		this.y0 = y0; this.y1 = y1;
	}
	public void draw(Graphics g, int xoffset, int yoffset, int height)
    {
		g.setColor(color);
		g.drawLine(x0+xoffset, height-y0-yoffset, x1+xoffset, height-y1-yoffset);
	}
}