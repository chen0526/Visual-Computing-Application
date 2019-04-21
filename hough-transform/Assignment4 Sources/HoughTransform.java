import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

// Main class
public class HoughTransform extends Frame implements ActionListener {
	BufferedImage input;
	int width, height, diagonal;
	ImageCanvas source, target;
	TextField texRad, texThres;
	// Constructor
	public HoughTransform(String name) {
		super("Hough Transform");
		// load image
		try {
			input = ImageIO.read(new File(name));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		diagonal = (int)Math.sqrt(width * width + height * height);
		// prepare the panel for two images.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		target = new ImageCanvas(input);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Line Transform");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Radius:"));
		texRad = new TextField("10", 3);
		controls.add(texRad);
		button = new Button("Circle Transform");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Threshold:"));
		texThres = new TextField("75", 3);
		controls.add(texThres);
		button = new Button("Search");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(diagonal*2+100, Math.max(height,360)+100);
		setVisible(true);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	// Action listener
	public void actionPerformed(ActionEvent e) {
		// perform one of the Hough transforms if the button is clicked.
		if ( ((Button)e.getSource()).getLabel().equals("Line Transform") ) {
			//BufferedImage img_new = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
			/*
			int[][] g = new int[360][diagonal];
			// insert your implementation for straight-line here.
			DisplayTransform(diagonal, 360, g);
			*/

			int threshold = Integer.parseInt(texThres.getText());
			int[][] hough_array = new int[diagonal][360];
			int[][] image_matrix = new int[height][width];
			int[][] g = new int[diagonal][360];

			//take the pixel value into the matrix
			for ( int x = 0; x < width; x++ ){
				for ( int y = 0; y < height; y++ ){
					Color clr = new Color(source.image.getRGB(y, x));
					image_matrix[y][x] = clr.getRed()<<16| clr.getGreen()<<8| clr.getBlue();
				}
			}

			//calculate the hough transform value
			for ( int theta = 0; theta < 360; theta++ ){
				for ( int x = 0; x < width; x++ ){
					for( int y = 0; y < width; y++ ){
						//pick the pixel that are not white
						if ( (image_matrix[y][x] & 0x0000ff ) < 255 ){
							int r = (int)(x*Math.cos(Math.toRadians(theta)) + y*Math.sin(Math.toRadians(theta)));
							if(r>0){
								hough_array[r][theta]++;
							}
						}
					}
				}
			}
			//new image
			BufferedImage bufImg = null;
			try {
				bufImg = ImageIO.read(new File("rectangle.png"));
			}
			catch ( Exception ex ) {
				ex.printStackTrace();
			}
			//convert to another type
			BufferedImage convertedImg = new BufferedImage(bufImg.getWidth(), bufImg.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
			convertedImg.getGraphics().drawImage(bufImg, 0, 0, null);
			Graphics2D l = convertedImg.createGraphics();
			l.drawImage(convertedImg, 0, 0, null);
			l.setColor(Color.RED);
			//calculate the point associated to the hough value.
			//use the point to draw line.
			for ( int theta = 0; theta < 360; theta++ ){
				for ( int r =0; r < diagonal; r++ ){
					if ( hough_array[r][theta] > threshold ) {
						int x0 = 0;
						int y0 = (int)((r-x0)/Math.sin(Math.toRadians(theta)));
						int x1 =  width -1;
						int y1 = (int)((r-x1*Math.cos(Math.toRadians(theta)))/Math.sin(Math.toRadians(theta)));
						if ( y1 < 0 || y1 > height ){
							y0 = 0;
							y1 = height - 1;
							x0 = (int)((r-y0)/Math.cos(Math.toRadians(theta)));
							x1 = (int)((r-y1*Math.sin(Math.toRadians(theta)))/Math.cos(Math.toRadians(theta)));;
						}
						// add points
						//Graphics l = source.image.createGraphics();
						//l.setColor(Color.RED);
						l.drawLine(x0,y0,x1,y1);
					}
				}
			}
			source.resetImage(convertedImg);
			g = hou_transform(image_matrix, g);
			DisplayTransform_line(360, diagonal, g);

		}
		else if ( ((Button)e.getSource()).getLabel().equals("Circle Transform") ) {
			int[][] g = new int[height][width];
            int[][] image_matrix = new int[height][width];
			int radius = Integer.parseInt(texRad.getText());
            int thres = Integer.parseInt(texThres.getText());
            int threshold = 75;
            int steps = 2 * radius;

            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    Color clr = new Color(source.image.getRGB(x, y));
                    float red = (float)clr.getRed()/255;
                    float green = (float)clr.getGreen()/255;
                    float blue = (float)clr.getBlue()/255;

                    float max = Math.max(Math.max(red, green), blue);
                    float min = Math.min(Math.min(red, green), blue);

                    int lumin = Math.round((max + min) / 2 * 100);
                    if (lumin < threshold){
                        // Black Pixel
                        for (float t = 0; t < Math.PI/4; t+=(Math.PI/(float)steps)){
                            int w = (int)Math.round((float)radius * Math.cos(t));
                            int h = (int)Math.round((float)radius * Math.sin(t));
                            // Top Half
                            if(y+h < height && x+w < width){
                                g[y+h][x+w]++;
                            }
                            if(y-h >= 0 && x+w < width){
                                g[y-h][x+w]++;
                            }
                            if(y-w >= 0 && x+h < width){
                                g[y-w][x+h]++;
                            }
                            if(y+w < height && x+h < width){
                                g[y+w][x+h]++;
                            }
                            // Bottom Half
                            if(y+h < height && x-w > 0){
                                g[y+h][x-w]++;
                            }
                            if(y-h > 0 && x-w > 0){
                                g[y-h][x-w]++;
                            }
                            if(y-w > 0 && x-h > 0){
                                g[y-w][x-h]++;
                            }
                            if(y+w < height && x-h > 0){
                                g[y+w][x-h]++;
                            }
                        }
                    }
                }
            }
            // Get the Max
            int max = 0;
            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    if(g[y][x] > max) max = g[y][x];
                }
            }
            // Find the factor of multiplication
            float factor = 100/ (float)max;

            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    g[y][x] = (int)Math.round((float)g[y][x] * factor);
                }
            }
			// insert your implementation for circle here.
			DisplayTransform(width, height, g);

            PaintCircles(g, radius, thres);

            source.repaint();
		}
	}
	// display the spectrum of the transform.
	public void DisplayTransform(int wid, int hgt, int[][] g) {
		target.resetBuffer(wid, hgt);
		for ( int y=0, i=0 ; y<hgt ; y++ )
			for ( int x=0 ; x<wid ; x++, i++ )
			{
				int value = g[y][x] > 255 ? 255 : g[y][x];
				target.image.setRGB(x, y, new Color(value, value, value).getRGB());
			}
		target.repaint();
	}

    public void PaintCircles(int[][] g, int r, int thres){
		//new image
			BufferedImage bufImg = null;
			try {
				bufImg = ImageIO.read(new File("HoughCircles3.png"));
			}
			catch ( Exception ex ) {
				ex.printStackTrace();
			}
			//convert to another type
			BufferedImage convertedImg = new BufferedImage(bufImg.getWidth(), bufImg.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
			convertedImg.getGraphics().drawImage(bufImg, 0, 0, null);
			Graphics2D l = convertedImg.createGraphics();
			l.drawImage(convertedImg, 0, 0, null);
			l.setColor(Color.RED);
			source.resetImage(convertedImg);

        int steps = r * 4;
        int[][] returner = new int[height][width];

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                // Above the thres
                if(g[y][x] >= thres){
                    for (float t = 0; t < Math.PI/4; t+=(Math.PI/(float)steps)){
                        int w = (int)Math.round((float)r * Math.cos(t));
                        int h = (int)Math.round((float)r * Math.sin(t));
                        // Top Half
                        if(y+h < height && x+w < width){
                            source.image.setRGB(y+h,x+w,Color.RED.getRGB());
                        }
                        if(y-h >= 0 && x+w < width){
                            source.image.setRGB(y-h,x+w,Color.RED.getRGB());
                        }
                        if(y-w >= 0 && x+h < width){
                            source.image.setRGB(y-w,x+h, Color.RED.getRGB());
                        }
                        if(y+w < height && x+h < width){
                            source.image.setRGB(y+w, x+h, Color.RED.getRGB());
                        }
                        // Bottom Half
                        if(y+h < height && x-w > 0){
                            source.image.setRGB(y+h, x-w, Color.RED.getRGB());
                        }
                        if(y-h > 0 && x-w > 0){
                            source.image.setRGB(y-h,x-w, Color.RED.getRGB());
                        }
                        if(y-w > 0 && x-h > 0){
                            source.image.setRGB(y-w, x-h, Color.RED.getRGB());
                        }
                        if(y+w < height && x-h > 0){
                            source.image.setRGB(y+w, x-h, Color.RED.getRGB());
                        }
                    }
                }
            }
        }

    }

		// display the spectrum of the transform.
	public void DisplayTransform_line(int wid, int hgt, int[][] g) {
		target.resetBuffer(hgt, wid);
		for ( int y=0, i=0; y<hgt ; y++ )
			for ( int x=0 ; x<wid ; x++, i++ )
			{
				int value = g[y][x] > 255 ? 255 : g[y][x];
				target.image.setRGB(y, x, new Color(value, value, value).getRGB());
			}
		target.repaint();
	}
		// hough calculation for g
	public int[][] hou_transform(int[][] image_matrix, int[][] g){
		int x_center = width/2;
		int y_center = height/2;
		double theta_c = Math.PI/360;
		for ( int theta = 0; theta < 360; theta++ ){
			for ( int x = 0; x < width; x++ ){
				for( int y = 0; y < width; y++ ){
					if ( (image_matrix[x][y] & 0x0000ff ) < 255 ){
						int r = (int)Math.round((x-x_center)*Math.cos(theta*theta_c) + (int)((y-y_center)*Math.sin(theta*theta_c)));
						int r_level= (int)Math.round(r+diagonal/2);
						if ( r_level < 0) {
							r_level = Math.abs(r_level);
						}
						g[r_level][theta]+=2;
					}
				}
			}
		}
		return g;
	}

	public static void main(String[] args) {
		new HoughTransform(args.length==1 ? args[0] : "rectangle.png");
	}
}
