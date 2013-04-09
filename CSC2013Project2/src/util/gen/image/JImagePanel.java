package util.gen.image;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class JImagePanel extends JPanel
{
	private Image image;

	public JImagePanel()
	{
		super();
	}

	public JImagePanel(Image image)
	{
		this();
		setImage(image);
	}

	public void setImage(Image image)
	{
		this.image = image;
		int width = this.image.getWidth(null);
		int height = this.image.getHeight(null);
		Dimension size = new Dimension(width, height);
		setPreferredSize(size);
		setSize(width, height);
	}

	@Override
	public void paintComponent(Graphics g)
	{
		Image image = this.image;
		if(image != null)
		{
			g.drawImage(image, 0, 0, null);
		}
	}

	private static final long serialVersionUID = -4740238532070211306L;
}