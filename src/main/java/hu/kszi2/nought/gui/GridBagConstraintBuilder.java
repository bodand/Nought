package hu.kszi2.nought.gui;

import org.jetbrains.annotations.Contract;

import java.awt.*;

/**
 * Builder class for {@link java.awt.GridBagConstraints} objects to allow creating
 * them inline, without the need for specifying all parameters.
 */
@SuppressWarnings("UnusedReturnValue")
public class GridBagConstraintBuilder {
    /**
     * Sets the internal padding to x and y.
     *
     * @param x Set ipadx to this value
     * @param y Set ipady to this value
     * @return The builder object
     */
    @Contract("_,_->this")
    public GridBagConstraintBuilder ipad(int x, int y) {
        ipadx(x);
        ipady(y);
        return this;
    }

    /**
     * Sets the internal padding in x to the given value
     *
     * @param x Set ipadx to this value
     * @return The builder object
     */
    @Contract("_->this")
    public GridBagConstraintBuilder ipadx(int x) {
        constraints.ipadx = x;
        return this;
    }

    /**
     * Sets the internal padding in y to the given value
     *
     * @param y Set ipady to this value
     * @return The builder object
     */
    @Contract("_->this")
    public GridBagConstraintBuilder ipady(int y) {
        constraints.ipady = y;
        return this;
    }

    /**
     * Sets the position in the grid to the cell at (x,y).
     *
     * @param x The x position on the grid (column)
     * @param y The y position on the grid (row)
     * @return The builder object
     */
    @Contract("_,_->this")
    public GridBagConstraintBuilder grid(int x, int y) {
        gridx(x);
        gridy(y);
        return this;
    }

    /**
     * Sets the column number on the grid (the x coordinate).
     *
     * @param x The x position on the grid (column)
     * @return The builder object
     */
    @Contract("_->this")
    public GridBagConstraintBuilder gridx(int x) {
        constraints.gridx = x;
        return this;
    }


    /**
     * Sets the column number on the grid (the y coordinate).
     *
     * @param y The y position on the grid (column)
     * @return The builder object
     */
    @Contract("_->this")
    public GridBagConstraintBuilder gridy(int y) {
        constraints.gridy = y;
        return this;
    }

    /**
     * Sets the span (width and height) of the object to be placed in the grid.
     * The placed object will take up {@code width} cells in the x direction and
     * {@code height} cells in the y direction.
     *
     * @param width  The amount of cells to take up horizontally
     * @param height The amount of cells to take up vertically
     * @return The builder object
     */
    @Contract("_,_->this")
    public GridBagConstraintBuilder gridspan(int width, int height) {
        gridwidth(width);
        gridheight(height);
        return this;
    }

    /**
     * Sets the width of the placed object in the grid, i.e. the amount of
     * cells it will consume horizontally.
     *
     * @param width The amount of cells to take up horizontally
     * @return The builder object
     */
    @Contract("_->this")
    public GridBagConstraintBuilder gridwidth(int width) {
        constraints.gridwidth = width;
        return this;
    }

    /**
     * Sets the height of the placed object in the grid, i.e. the amount of
     * cells it will consume vertically.
     *
     * @param height The amount of cells to take up vertically
     * @return The builder object
     */
    @Contract("_->this")
    public GridBagConstraintBuilder gridheight(int height) {
        constraints.gridheight = height;
        return this;
    }

    /**
     * Sets the weight of the object in the cells of the builder.
     * The weight is mainly used for resizing calculations among others,
     * see the {@link GridBagLayout} documentation for it is not trivial.
     *
     * @param x The x weight of the object
     * @param y The y weight of the object
     * @return The builder object
     */
    @Contract("_,_->this")
    public GridBagConstraintBuilder weight(double x, double y) {
        weightx(x);
        weighty(y);
        return this;
    }

    /**
     * Sets the x weight of the placed object's cell in the grid.
     *
     * @param x The x weight of the object's cell
     * @return The builder object
     */
    @Contract("_->this")
    public GridBagConstraintBuilder weightx(double x) {
        constraints.weightx = x;
        return this;
    }

    /**
     * Sets the y weight of the placed object's cell in the grid.
     *
     * @param y The y weight of the object's cell
     * @return The builder object
     */
    @Contract("_->this")
    public GridBagConstraintBuilder weighty(double y) {
        constraints.weighty = y;
        return this;
    }

    /**
     * Sets the insets object for external padding in the cell.
     *
     * @param top    The top padding
     * @param left   The left padding
     * @param bottom The bottom padding
     * @param right  The right padding
     * @return The builder object
     */
    @Contract("_,_,_,_->this")
    public GridBagConstraintBuilder insets(int top, int left, int bottom, int right) {
        return insets(new Insets(top, left, bottom, right));
    }

    /**
     * Sets the insets object for external padding in the cell.
     *
     * @param insets The external padding in the cell
     * @return The builder object
     */
    @Contract("_->this")
    public GridBagConstraintBuilder insets(Insets insets) {
        constraints.insets = insets;
        return this;
    }

    /**
     * Sets the anchor type to the provided.
     * Depending on the type, the object will be pinned the correct corner or
     * side of the cells it takes up.
     *
     * @param type The orientation of the pinning
     * @return The builder object
     */
    @Contract("_->this")
    public GridBagConstraintBuilder anchor(int type) {
        constraints.anchor = type;
        return this;
    }

    /**
     * Sets the fill type of the placed object.
     * The object will be stretched to fill the grid cells it takes up the
     * direction specified in the type: NONE/VERTICAL/HORIZONTAL/BOTH.
     *
     * @param type The type of the fill
     * @return The builder object
     */
    @Contract("_->this")
    public GridBagConstraintBuilder fill(int type) {
        constraints.fill = type;
        return this;
    }

    /**
     * Returns the constraint set which is currently being built.
     * After this call, the internal object for building is reset to its defaults
     * which is a different behavior to {@link hu.kszi2.nought.core.TodoBuilder},
     * although it makes more sense.
     *
     * @return The newly built {@link GridBagConstraints} object
     */
    @Contract("->new")
    public GridBagConstraints build() {
        var ret = constraints;
        constraints = new GridBagConstraints();
        return ret;
    }

    private GridBagConstraints constraints = new GridBagConstraints();
}
