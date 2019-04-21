import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JFrame;


/*
    Group 19
    Assignment5 Comp3301
    Nicholas Atkins 201509098
    Meheroon Nesa Tondra 201555661
    Yusong Du 201458700
*/

// Main class
public class CornerDetection extends Frame implements ActionListener {
	BufferedImage input;
	int width, height;
	double sensitivity=.1;
	int threshold=20;
	boolean TargetImageChanged = false;
	
	//2D array to hold the pixel value of an image
	double[][] Image;
	//ArrayList to hold the x coordinate of a pixel value
	ArrayList<Integer> x_points;
	//ArrayList to hold the y coordinate of a pixel value
	ArrayList<Integer> y_points;

	ImageCanvas source, target;
	CheckboxGroup metrics = new CheckboxGroup();

	// Constructor
	public CornerDetection(String name) {
		super("Corner Detection");
		// load image
		try {
			input = ImageIO.read(new File(name));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();

		// set up array and list
		Image = new double[height][width];
		x_points = new ArrayList<>();
		y_points = new ArrayList<>();

		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		target = new ImageCanvas(width, height);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);

		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Derivatives");
		button.addActionListener(this);
		controls.add(button);

		// Use a slider to change sensitivity
		JLabel label1 = new JLabel("sensitivity=" + sensitivity);
		controls.add(label1);
		JSlider slider1 = new JSlider(1, 25, (int)(sensitivity*100));
		slider1.setPreferredSize(new Dimension(50, 20));
		controls.add(slider1);
		slider1.addChangeListener(changeEvent -> {
			sensitivity = slider1.getValue() / 100.0;
			label1.setText("sensitivity=" + (int)(sensitivity*100)/100.0);
		});
		button = new Button("Corner Response");
		button.addActionListener(this);
		controls.add(button);
		JLabel label2 = new JLabel("threshold=" + threshold);
		controls.add(label2);
		JSlider slider2 = new JSlider(0, 100, threshold);
		slider2.setPreferredSize(new Dimension(50, 20));
		controls.add(slider2);
		slider2.addChangeListener(changeEvent -> {
			threshold = slider2.getValue();
			label2.setText("threshold=" + threshold);
		});
		button = new Button("Thresholding");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Non-max Suppression");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Display Corners");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(Math.max(width*2+100,850), height+110);
		setVisible(true);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {
		// generate Moravec corner detection result
		if ( ((Button)e.getSource()).getLabel().equals("Derivatives") ){
			derivatives();
		}
		// perform non-max suppression and display the detected corner pixels
		if ( ((Button)e.getSource()).getLabel().equals("Non-max Suppression") ){
			BufferedImage img_new = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			nonMaxSuppression();
			double r, g, b;
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					r = clamp(Image[y][x]);
					g = clamp(Image[y][x]);
					b = clamp(Image[y][x]);
					//System.out.println("r: "+r+" "+"g: "+g+" "+"b: "+b);
					img_new.setRGB(y, x, new Color((int)r,(int)g,(int)b).getRGB());
				}
			}
			target.resetImage(img_new);
		}
		
		// calculate corner response under user specified sensitivity
		// display a result as a gray scale image
		if ( ((Button)e.getSource()).getLabel().equals("Corner Response") ){
			BufferedImage img_new = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			cornerResponse();
			
			double r, g, b;
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					r = clamp(Image[y][x]);
					g = clamp(Image[y][x]);
					b = clamp(Image[y][x]);
					img_new.setRGB(y, x, new Color((int)r,(int)g,(int)b).getRGB() );
				}
			}
			target.resetImage(img_new);
			TargetImageChanged = true;
		}
		else if(((Button)e.getSource()).getLabel().equals("Thresholding") ) {
			if(TargetImageChanged) {
				//continue
			}
			else {
				JFrame frame = new JFrame();
				JOptionPane.showMessageDialog(frame, "Please click on Corner Response");
				return;
			}
			
			int thres = threshold * (255/100);
			System.out.println(thres);
			//initialize			
			BufferedImage img_new = new BufferedImage(width, height, target.image.getType());
			int r, g, b, value;
			int vr =0;
			int vg = 0;
			int vb = 0;
			for(int p=0; p<width; p++) {
				for(int q=0; q<height; q++) {
					//
					Color clr = new Color(target.image.getRGB(p,q));
					r = clr.getRed();
					g = clr.getGreen();
					b = clr.getBlue();
					int alpha = new Color(target.image.getRGB(p, q)).getAlpha();
					
					if(r > thres) {
						vr = 255;
					}
                    else{
                        vr = 0;
                    }
					if(g > thres) {
						vg = 255;
					}
                    else{
                        vg = 0;
                    }
					if(b > thres) {
						vb = 255;
					}
					else {
						vb = 0;
					}
					value = GetRGB(alpha, vr, vg, vg);
					img_new.setRGB(p, q, value);
				}
			}
			target.resetImage(img_new);
		}
		else if(((Button)e.getSource()).getLabel().equals("Display Corners") ) 
		{
			int r = 5; // radius of circle
			int r0 = 1; // radius of centre
			Graphics2D gr = input.createGraphics();
			gr.drawImage(input, 0, 0, null);
			gr.setClip(null);
			Stroke s = new BasicStroke(2);
			gr.setStroke(s);
			gr.setColor(new Color(255, 0, 0));
			Color red = new Color(255, 0, 0);
			for(int p=0; p<width; p++) {
				for(int q=0; q<height; q++) {
					Color clr = new Color(target.image.getRGB(p,q));
					int red2 = clr.getRed();
					if(red2 != 0) {
						int x = p - r/2;
						int y = q - r/2;
						//dot for the centre of the circle
						//gr.drawOval(x+r/2, y+r/2, r0, r0);
						//circle circumference
						gr.drawOval(x, y, r, r);
					}
				}
			}
			source.resetImage(input);
		}
	}
	public static void main(String[] args) {
		new CornerDetection(args.length==1 ? args[0] : "signal_hill.png");
	}

	// calculate three derivatives products and display it usig three color channels (x, y, xy)
	void derivatives() {

		// set up sample 2d guassian data at grid
		int[] gaussianX  = { -1, -2 , 0, 2 , 1, 
						     -4, -10, 0, 10, 4, 
						     -7, -17, 0, 17, 7,
						     -4, -10, 0, 10, 4,
						     -1, -2 , 0, 2 , 1 };
		
		int[] gaussianY = { -1, -4 , -7 , -4 , -1,
				            -2, -10, -17, -10, -2, 
				             0,  0 ,  0 ,  0 ,  0, 
				             2,  10,  17,  10,  2,
				             1,  4 ,  7 ,  4 ,  1 };
		
		int[] gaussianXY = { 1, 4 , 6 , 4 , 1,
							 4, 16, 24, 16, 1,
							 6, 24, 36, 24, 6, 
							 4, 16, 24, 16, 4,
							 1, 4 , 6 , 4 , 1 };
		
		int pos = 2;
		int gray;
		int[] tempx = new int[25];
		int[] tempy = new int[25];
		int[] tempxy = new int[25];
		int temp_dx = 0;
		int temp_dy = 0;
		int temp_dxy = 0;
		double dx, dy, dxy;
		double dx2, dy2;
		
		for ( int q = pos; q < height - pos; q++ ){
			for ( int p = pos; p < width - pos; p++ ){
				int i = 0;
				temp_dx = 0;
				temp_dy = 0;
				temp_dxy = 0;
				for ( int v = -pos; v <= pos; v++ ){
					for ( int u = -pos; u <= pos; u++ ){
						Color clr = new Color(source.image.getRGB(q+v,p+u));
						gray = (clr.getRed() + clr.getGreen() + clr.getBlue())/3;
						tempx[i] = gray*gaussianX[i];
						tempy[i] = gray*gaussianY[i];
						tempxy[i] = gray*gaussianXY[i];
						i++;
					}
				}
				for ( int t = 0; t < gaussianX.length; t++ ){
					temp_dx += tempx[t];
				}
				for ( int t = 0; t < gaussianY.length; t++ ){
					temp_dy += tempy[t];
				}
				for ( int t = 0; t < gaussianXY.length; t++ ){
					temp_dxy += tempxy[t];
				}

				dx = temp_dx/58;
				dx2 = dx*dx*0.05;

				if ( dx2 > 255 ) { 
					dx2 = 255; 
				}

				dy = temp_dy/58;
				dy2 = dy*dy*0.05;

				if ( dy2 > 255 ) { 
					dy2 = 255; 
				}
				dxy = dx*dy*0.09;
				if ( dxy < 0 ) { 
					dxy = 0; 
				}
				if ( dxy > 255 ) { 
					dxy = 255; 
				}
				target.image.setRGB(q, p, new Color((int)dx2, (int)dy2, (int)dxy).getRGB());
			}
		}
		target.repaint();
	}

	// clamp the pixel value between 0 and 255 
	double clamp(double t){
		if ( t < 0 ){
			 t = 0; 
		}
		if ( t > 255 ){ 
			t = 255; 
		}
		return t;
	}

	void nonMaxSuppression(){
		int suppression = 3;
		int xIndex = 0;
		int yIndex = 0;
		
		for ( int y = suppression, maxY = height - y; y < maxY; y++ ){
			for ( int x = suppression, maxX = width - x; x < maxX; x++ ){
				double currentValue = Image[y][x];
				//System.out.println(currentValue);
				double max = Image[y][x];
				for ( int i = -suppression; (currentValue != 0) && (i <= suppression); i++ ){
					for ( int j = -suppression; j <= suppression; j++ ){
						
						if ( Image[y+i][x+j] < currentValue ){
							Image[y+i][x+j] = 0;
						}
						else if ( Image[y+i][x+j] > currentValue){
							xIndex = x+j;
							yIndex = y+i;
						}
					}
				}
				//System.out.println(xIndex);
				//System.out.println(yIndex);
				x_points.add(yIndex);
				y_points.add(xIndex);
			}
		}
	}

	// calculate corner response value
	void cornerResponse(){
		initializeImage();
		int[] gaussianX  = { -1, -2 , 0, 2 , 1, 
			     			 -4, -10, 0, 10, 4, 
			     			 -7, -17, 0, 17, 7,
			     			 -4, -10, 0, 10, 4,
			     			 -1, -2 , 0, 2 , 1 };

		int[] gaussianY = { -1, -4 , -7 , -4 , -1,
	            			-2, -10, -17, -10, -2, 
	            			 0,  0 ,  0 ,  0 ,  0, 
	            			 2,  10,  17,  10,  2,
	            			 1,  4 ,  7 ,  4 ,  1 };

		int[] gaussianXY = { 1, 4 , 6 , 4 , 1,
				 			 4, 16, 24, 16, 1,
				 			 6, 24, 36, 24, 6, 
				 			 4, 16, 24, 16, 4,
				 			 1, 4 , 6 , 4 , 1 };

		double[] A = new double[4];
		
		int pos = 2;
		double R;
		Color clr;
		double gray;
		double[] tempx = new double[25];
		double[] tempy = new double[25];
		double[] tempxy = new double[25];
		double temp_dx = 0;
		double temp_dy = 0;
		double temp_dxy = 0;
		double dx, dy, dxy;
		double dx2, dy2;
		
		for ( int q = pos; q < height - pos; q++ ){
			for ( int p = pos; p < width - pos; p++ ){
				int i = 0;
				temp_dx = 0;
				temp_dy = 0;
				temp_dxy = 0;
				for ( int v = -pos; v <= pos; v++ ){
					for ( int u = -pos; u <= pos; u++ ){
						clr = new Color(source.image.getRGB(q+v,p+u));
						gray = (clr.getRed() + clr.getGreen() + clr.getBlue())/3;
						tempx[i] = gray*gaussianX[i];
						tempy[i] = gray*gaussianY[i];
						tempxy[i] = gray*gaussianXY[i];
						i++;
					}
				}
				for ( int t = 0; t < gaussianX.length; t++ ){
					temp_dx += tempx[t];
				}
				for ( int t = 0; t < gaussianY.length; t++ ){
					temp_dy += tempy[t];
				}
				for ( int t = 0; t < gaussianXY.length; t++ ){
					temp_dxy += tempxy[t];
				}
				dx = (temp_dx/58);
				dx2 = dx*dx;
				//System.out.println("dx2: "+dx2);
				if ( dx2 > 255 ) { dx2 = 255; }
				dy = (temp_dy/58);
				dy2 = dy*dy;
				//System.out.println("dy2: "+dy2);
				if ( dy2 > 255 ) { dy2 = 255; }
				dxy = temp_dxy/256;
				//System.out.println("dxy: "+dxy);
				if ( dxy < 0 ) { dxy = 0; }
				A[0] = dx2;
				A[1] = dxy;
				A[2] = dxy;
				A[3] = dy2;
				R = ((A[0]*A[3]-A[1]*A[2]) - sensitivity*Math.pow(A[0]+A[3], 2));
				if ( R < 0 ){ R = 0; }
				Image[q][p] = R;
			}
		}
		scaleResponseValue();
	}

	// scale the corner response value
	void scaleResponseValue(){
		double mean = findMean();
		double scale = 255/mean;
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
					Image[y][x] = (int)(Image[y][x]*0.01);
			}
		}
	}
	
	// find mean value for the image pixels
	double findMean(){
		double sum = 0;
		double mean = 0;
		double count = 0;
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				if ( Image[y][x] > 0){
					sum += Image[y][x];
					count++;
				}
			}
		}
		mean = sum/count;
		return mean;
	}

    //initialize all the pixel value of an image to 0
	void initializeImage(){
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				Image[y][x] = 0;
			}
		}
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

}
