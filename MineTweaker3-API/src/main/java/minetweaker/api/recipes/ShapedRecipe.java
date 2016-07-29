/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package minetweaker.api.recipes;

import java.util.HashMap;
import java.util.Map;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.player.IPlayer;

/**
 *
 * @author Stan
 */
public class ShapedRecipe implements ICraftingRecipe {
	private final int width;
	private final int height;
	private final byte[] colPos;
	private final byte[] rowPos;
	private final boolean mirrored;
	private final IRecipeFunction function;

	private final IItemStack output;
	private final IIngredient[] ingredients;

	public ShapedRecipe(IItemStack output, IIngredient[][] ingredients, IRecipeFunction function, boolean mirrored) {
		
		int height = 0;
		int width = 0;		
		int thisRowWidth = 0;
		int numIngredients = 0;
		IIngredient[] temp = new IIngredient[9];  //max size!

		for (IIngredient[] row : ingredients) {
			height ++;
			thisRowWidth = 0;
			for (IIngredient ing : row) {
				temp[numIngredients] = ing;
				thisRowWidth ++;
				numIngredients ++;
			}				
			if (thisRowWidth > width) {
				width = thisRowWidth;
			}
		}
		
		IIngredient[] finalIng = new IIngredient[numIngredients];  //max size!		
		if (numIngredients < 9){
			for(int i = 0; i < numIngredients; i++) {
				finalIng[i] = temp[i];				
			}
		}
		else{
			finalIng = temp;			
		}
		
		this.colPos = new byte[numIngredients];
		this.rowPos = new byte[numIngredients];
		this.output = output;
		this.ingredients = finalIng;
		this.function = function;
		this.height = height;
		this.width = width;
		this.mirrored = mirrored;

		int ix = 0;
		for (int row = 0; row < this.height; row++) {
			for (int col = 0; col < this.width; col++) {
				this.colPos[ix] = (byte) col;
				this.rowPos[ix] = (byte) row;
				ix++;
			}
		}
		
	}

	public ShapedRecipe(IItemStack output, IIngredient[] ingredients, int width, int height, IRecipeFunction function, boolean mirrored) {
		int numIngredients = ingredients.length;
		this.colPos = new byte[numIngredients];
		this.rowPos = new byte[numIngredients];
		this.output = output;
		this.ingredients = ingredients;
		this.function = function;
		this.height = height;
		this.width = width;
		this.mirrored = mirrored;

		int ix = 0;
		for (int row = 0; row < this.height; row++) {
			for (int col = 0; col < this.width; col++) {
				this.colPos[ix] = (byte) col;
				this.rowPos[ix] = (byte) row;
				ix++;
			}
		}		
		
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isMirrored() {
		return mirrored;
	}

	public IIngredient[] getIngredients() {
		return ingredients;
	}

	public byte[] getIngredientCols() {
		return colPos;
	}

	public byte[] getIngredientRows() {
		return rowPos;
	}

	/**
     * Returns the Ingredient in the slot specified (Top left is 0, 0). Args: row, column
     */
    public IIngredient getIngredientAt(int row, int col)
    {
        if (row >= 0 && row < getHeight())
        {
            int k = col + row * getWidth();
            return getIngredients()[k];
        }
        else
        {
            return null;
        }
    }

	public IItemStack getOutput() {
		return output;
	}

	@Override
	public boolean matches(ICraftingInventory inventory) {
		if (inventory.getStackCount() != ingredients.length) {
			return false;
		}

		for (int i = 0; i <= inventory.getWidth() - width; i++) {
			out: for (int j = 0; j <= inventory.getHeight() - height; j++) {
				for (int k = 0; k < ingredients.length; k++) {
					IItemStack item = inventory.getStack(colPos[k] + i, rowPos[k] + j);
					if (item == null)
						continue out;

					if (!ingredients[k].matches(item))
						continue out;
				}

				return true;
			}
		}

		if (mirrored) {
			for (int i = 0; i <= inventory.getWidth() - width; i++) {
				out: for (int j = 0; j <= inventory.getHeight() - height; j++) {
					for (int k = 0; k < ingredients.length; k++) {
						IItemStack item = inventory.getStack(inventory.getWidth() - (colPos[k] + i) - 1, rowPos[k] + j);
						if (item == null)
							continue out;

						if (!ingredients[k].matches(item))
							continue out;
					}

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public IItemStack getCraftingResult(ICraftingInventory inventory) {
		IItemStack[] stacks = new IItemStack[ingredients.length];

		for (int i = 0; i <= inventory.getWidth() - width; i++) {
			out: for (int j = 0; j <= inventory.getHeight() - height; j++) {
				for (int k = 0; k < ingredients.length; k++) {
					IItemStack item = inventory.getStack(colPos[k] + i, rowPos[k] + j);
					if (item == null)
						continue out;

					if (!ingredients[k].matches(item))
						continue out;
					stacks[k] = item;
				}

				return doRecipe(inventory, stacks, i, j, output, false);
			}
		}

		if (mirrored) {
			for (int i = 0; i <= inventory.getWidth() - width; i++) {
				out: for (int j = 0; j <= inventory.getHeight() - height; j++) {
					for (int k = 0; k < ingredients.length; k++) {
						IItemStack item = inventory.getStack(inventory.getWidth() - (colPos[k] + i) - 1, rowPos[k] + j);
						if (item == null)
							continue out;

						if (!ingredients[k].matches(item))
							continue out;
						stacks[k] = item;
					}

					return doRecipe(inventory, stacks, i, j, output, true);
				}
			}
		}

		return null;
	}

	@Override
	public boolean hasTransformers() {
		for (IIngredient ingredient : ingredients) {
			if (ingredient != null && ingredient.hasTransformers()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void applyTransformers(ICraftingInventory inventory, IPlayer byPlayer) {
		IItemStack[] stacks = new IItemStack[ingredients.length];

		for (int i = 0; i <= inventory.getWidth() - width; i++) {
			out: for (int j = 0; j <= inventory.getHeight() - height; j++) {
				for (int k = 0; k < ingredients.length; k++) {
					IItemStack item = inventory.getStack(colPos[k] + i, rowPos[k] + j);
					if (item == null)
						continue out;

					if (!ingredients[k].matches(item))
						continue out;
					stacks[k] = item;
				}

				doRecipeTransformers(inventory, stacks, i, j, false, byPlayer);
				return;
			}
		}

		if (mirrored) {
			for (int i = 0; i <= inventory.getWidth() - width; i++) {
				out: for (int j = 0; j <= inventory.getHeight() - height; j++) {
					for (int k = 0; k < ingredients.length; k++) {
						IItemStack item = inventory.getStack(inventory.getWidth() - (colPos[k] + i) - 1, rowPos[k] + j);
						if (item == null)
							continue out;

						if (!ingredients[k].matches(item))
							continue out;
						stacks[k] = item;
					}

					doRecipeTransformers(inventory, stacks, i, j, true, byPlayer);
					return;
				}
			}
		}
	}

	@Override
	public String toCommandString() {
		StringBuilder result = new StringBuilder();
		result.append("recipes.addShaped(");
		result.append(output);
		if (output.getAmount() > 1)
		{
			//Add output stack size
			result.append(" * " + output.getAmount());
		}
		result.append(", [");

		for (int row = 0; row < getHeight(); row++) {
			if (row > 0) {
				result.append(", ");
			}

			result.append("[");

			for (int col = 0; col < getWidth(); col++) {
				if (col > 0)
					result.append(", ");
			
					result.append(getIngredientAt(row, col));
			}

			result.append("]");
		}

		result.append("]);");
		return result.toString();
	}

	private IItemStack doRecipe(
			ICraftingInventory inventory,
			IItemStack[] stacks,
			int offx, int offy,
			IItemStack output,
			boolean mirrored) {
		// determine output and apply transformations

		if (function != null) {
			Map<String, IItemStack> tagged = new HashMap<String, IItemStack>();
			for (int k = 0; k < ingredients.length; k++) {
				if (ingredients[k].getMark() != null) {
					tagged.put(ingredients[k].getMark(), stacks[k]);
				}
			}

			// TODO: supply dimension
			output = function.process(output, tagged, new CraftingInfo(inventory, null));
			System.out.println("Ouput: " + output);
		}

		if (output == null) {
			return null;
		}

		/*
		 * for (int i = 0; i < ingredients.length; i++) { IItemStack transformed
		 * = ingredients[i].applyTransform(stacks[i]); if (transformed !=
		 * stacks[i]) { if (mirrored) { inventory.setStack( inventory.getWidth()
		 * - (offx + posx[i]) - 1, offy + posy[i], transformed); } else {
		 * inventory.setStack( offx + posx[i], offy + posy[i], transformed); } }
		 * }
		 */

		return output;
	}

	private void doRecipeTransformers(
			ICraftingInventory inventory,
			IItemStack[] stacks,
			int offx, int offy,
			boolean mirrored,
			IPlayer byPlayer) {
		for (int i = 0; i < ingredients.length; i++) {
			IItemStack transformed = ingredients[i].applyTransform(stacks[i], byPlayer);
			if (transformed != stacks[i]) {
				if (mirrored) {
					inventory.setStack(
							inventory.getWidth() - (offx + colPos[i]) - 1,
							offy + rowPos[i],
							transformed);
				} else {
					inventory.setStack(
							offx + colPos[i],
							offy + rowPos[i],
							transformed);
				}
			}
		}
	}
}
