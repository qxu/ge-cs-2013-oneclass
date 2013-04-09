package util.gen.image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class ImageDataByteArrayGenerator
{
	public static void main(String[] args) throws IOException
	{
		final BufferedImage blackImage = ImageIO.read(new File(
				"res\\tiles\\black.png"));
		final BufferedImage emptyImage = ImageIO.read(new File(
				"res\\tiles\\empty.png"));
		final BufferedImage lockImage = ImageIO.read(new File(
				"res\\tiles\\lock.png"));
		final BufferedImage questionImage = ImageIO.read(new File(
				"res\\tiles\\question.png"));
		final BufferedImage keyImage = ImageIO.read(new File(
				"res\\tiles\\mykey.png"));
		final BufferedImage exitImage = ImageIO.read(new File(
				"res\\tiles\\exit.png"));
		final BufferedImage locationImage = ImageIO.read(new File(
				"res\\tiles\\location.png"));

//		generateAndPrint(blackImage, "black");
//		generateAndPrint(lockImage, "lock");
//		generateAndPrint(exitImage, "exit");
//		generateAndPrint(keyImage, "key");
//		generateAndPrint(emptyImage, "empty");
//		generateAndPrint(questionImage, "question");
//		generateAndPrint(locationImage, "location");
		
		Image[][] tiles = 
			{
				{emptyImage, blackImage},
				{keyImage, lockImage},
				{locationImage, exitImage},
				{questionImage},
			};
		BufferedImage spriteSheet = tile(tiles, 2, 4, 16, 16);
		generateAndPrint(spriteSheet, "sprites");
	}
	
	private static BufferedImage tile(Image[][] tiles, int cols, int rows, int tileWidth, int tileHeight)
	{
		BufferedImage buf = new BufferedImage(cols * tileWidth, rows * tileHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = buf.createGraphics();

		for(int i = 0; i < tiles.length; ++i)
		{
			for(int j  = 0; j < tiles[i].length; ++j)
			{
				Image img = tiles[i][j];
				if(img != null)
				{
					g2d.drawImage(img, j * tileWidth, i * tileHeight, null);
				}
			}
		}
		g2d.dispose();
		return buf;
	}
	
	private static void showImage(Image image)
	{
		JFrame frame = new JFrame("test");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.add(new JImagePanel(image));
		frame.pack();
		frame.setVisible(true);
	}

	private static void generateAndPrint(RenderedImage image, String name)
	{
		ByteArrayOutputStream baout = new ByteArrayOutputStream();

		try
		{
			ImageIO.write(image, "gif", baout);
		}
		catch(IOException e)
		{
			throw new AssertionError(e);
		}

		byte[] data = baout.toByteArray();

		String s = Arrays.toString(data);
		s = s.replace('[', '{');
		s = s.replace("]", "};");
		System.out.println("private static final byte[] " + name + "Data = ");
		System.out.println(wrap(s, 40, "\n"));
		System.out.println();

//		showImage((Image)image);
	}

	private static String wrap(String str, int wrapLength, String newLineStr)
	{
		if(str == null)
		{
			return null;
		}
		if(newLineStr == null)
		{
			newLineStr = System.getProperty("line.separator");
		}
		if(wrapLength < 1)
		{
			wrapLength = 1;
		}
		int inputLineLength = str.length();
		int offset = 0;
		StringBuffer wrappedLine = new StringBuffer(inputLineLength + 32);

		while((inputLineLength - offset) > wrapLength)
		{
			if(str.charAt(offset) == ' ')
			{
				offset++;
				continue;
			}
			int spaceToWrapAt = str.lastIndexOf(' ', wrapLength + offset);

			if(spaceToWrapAt >= offset)
			{
				// normal case
				wrappedLine.append(str.substring(offset, spaceToWrapAt));
				wrappedLine.append(newLineStr);
				offset = spaceToWrapAt + 1;

			}
			else
			{
				spaceToWrapAt = str.indexOf(' ', wrapLength + offset);
				if(spaceToWrapAt >= 0)
				{
					wrappedLine
							.append(str.substring(offset, spaceToWrapAt));
					wrappedLine.append(newLineStr);
					offset = spaceToWrapAt + 1;
				}
				else
				{
					wrappedLine.append(str.substring(offset));
					offset = inputLineLength;
				}
			}
		}

		// Whatever is left in line is short enough to just pass through
		wrappedLine.append(str.substring(offset));

		return wrappedLine.toString();
	}

	private ImageDataByteArrayGenerator()
	{
	}
}