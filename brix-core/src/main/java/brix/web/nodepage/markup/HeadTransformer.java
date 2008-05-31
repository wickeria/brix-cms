package brix.web.nodepage.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Matej Knopp
 */
public class HeadTransformer extends MarkupSourceTransformer
{
	public HeadTransformer(MarkupSource delegate)
	{
		super(delegate);
	}

	private boolean isHead(Tag tag)
	{
		String name = tag.getName();
		return "head".equals(name) || "wicket:head".equals(name);
	}

	protected List<Item> extractHeadContent(List<Item> items)
	{
		List<Item> result = new ArrayList<Item>();

		int headDepth = 0;

		for (Item i : items)
		{
			if (i instanceof Tag)
			{
				Tag tag = (Tag) i;
				if (isHead(tag))
				{
					if (tag.getType() == Tag.Type.OPEN)
					{
						++headDepth;
					}
					else if (tag.getType() == Tag.Type.CLOSE)
					{
						if (headDepth > 0)
						{
							--headDepth;
						}
					}
					continue;
				}
			}
			if (headDepth > 0)
			{
				result.add(i);
			}
		}

		return result;
	}

	/**
	 * Transform the given list of original items. If headContent is not null,
	 * the items from head content are placed in the first head section of
	 * originalItems. All other head sections are removed. If head content is
	 * null, this method removes all head sections from original items. This
	 * method also removes duplicate &lt;body&gt; tags.
	 * 
	 * @param originalItems
	 * @param headContent
	 * @return
	 */
	protected List<Item> transform(List<Item> originalItems, List<Item> headContent)
	{
		List<Item> result = new ArrayList<Item>();

		// do not add head section if the headContent is null
		boolean wasHead = headContent == null;
		int headDepth = 0;
		int bodyDepth = 0;

		for (Item i : originalItems)
		{
			if (i instanceof Tag)
			{
				Tag tag = (Tag) i;
				
				if (wasHead == false && (isHead(tag) || "body".equals(tag.getName())))
				{
					Map<String, String> emptyMap = Collections.emptyMap();
					result.add(new SimpleTag("head", Tag.Type.OPEN, emptyMap));
					result.addAll(headContent);
					result.add(new SimpleTag("head", Tag.Type.CLOSE, null));
					if ("body".equals(tag.getName()))
					{
						bodyDepth++;
						result.add(tag);
					}
					wasHead = true;
					continue;
				}
				
				if ("body".equals(tag.getName()))
				{
					if (tag.getType() == Tag.Type.OPEN)
					{
						if (bodyDepth == 0)
						{
							result.add(tag);
						}
						++bodyDepth;
					}
					else if (tag.getType() == Tag.Type.CLOSE)
					{
						--bodyDepth;
						if (bodyDepth == 0)
						{
							result.add(tag);
						}
					}
					continue;
				}
				
				if (isHead(tag))
				{
					if (tag.getType() == Tag.Type.OPEN)
					{
						++headDepth;
					}
					else if (tag.getType() == Tag.Type.CLOSE)
					{
						if (headDepth > 0)
						{
							--headDepth;
						}
					}
					continue;
				}
			}
			if (headDepth == 0)
			{
				result.add(i);
			}
		}

		return result;
	}

	@Override
	protected List<Item> transform(List<Item> originalItems)
	{
		List<Item> headContent = extractHeadContent(originalItems);
		return transform(originalItems, headContent);
	}
}