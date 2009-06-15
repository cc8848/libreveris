//----------------------------------------------------------------------------//
//                                                                            //
//                                 S t i c k                                  //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2009. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Please contact users@audiveris.dev.java.net to report bugs & suggestions. //
//----------------------------------------------------------------------------//
//
package omr.stick;

import omr.glyph.Glyph;
import omr.glyph.GlyphSection;

import omr.lag.Lag;
import omr.lag.Run;
import omr.lag.ui.SectionView;

import omr.log.Logger;

import omr.math.BasicLine;
import omr.math.Line;

import omr.score.common.PixelPoint;

import java.awt.*;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.*;

/**
 * Class <code>Stick</code> describes a stick, a special kind of glyph, either
 * horizontal or vertical, as an aggregation of sections. Besides usual
 * positions and coordinates, a stick exhibits its approximating Line which is
 * the least-square fitted line on all points contained in the stick.
 *
 * <ul> <li> Staff lines, ledgers, alternate ends are examples of horizontal
 * sticks </li>
 *
 * <li> Bar lines, stems are examples of vertical sticks </li> </ul>
 *
 * @author Herv&eacute; Bitteur
 * @version $Id$
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "stick")
public class Stick
    extends Glyph
{
    //~ Static fields/initializers ---------------------------------------------

    /** Usual logger utility */
    private static final Logger logger = Logger.getLogger(Stick.class);

    //~ Instance fields --------------------------------------------------------

    /** Best line equation */
    private Line line;

    //~ Constructors -----------------------------------------------------------

    //-------//
    // Stick //
    //-------//
    /**
     * Create a stick with the related interline value
     * @param interline the very important scaling information
     */
    public Stick (int interline)
    {
        super(interline);
    }

    //-------//
    // Stick //
    //-------//
    /**
     * No-arg constructor to please JAXB
     */
    private Stick ()
    {
    }

    //~ Methods ----------------------------------------------------------------

    //------------------//
    // getAlienPixelsIn //
    //------------------//
    /**
     * Report the number of pixels found in the specified rectangle that do not
     * belong to the stick.
     *
     * @param area the rectangular area to investigate, in (coord, pos) form
     *
     * @return the number of alien pixels found
     */
    public int getAlienPixelsIn (Rectangle area)
    {
        int                      count = 0;
        final int                posMin = area.y;
        final int                posMax = (area.y + area.height) - 1;
        final List<GlyphSection> neighbors = lag.getSectionsIn(area);

        for (GlyphSection section : neighbors) {
            // Keep only sections that are not part of the stick
            if (section.getGlyph() != this) {
                int pos = section.getFirstPos() - 1; // Ordinate for horizontal,
                                                     // Abscissa for vertical

                for (Run run : section.getRuns()) {
                    pos++;

                    if (pos > posMax) {
                        break;
                    }

                    if (pos < posMin) {
                        continue;
                    }

                    int coordMin = Math.max(area.x, run.getStart());
                    int coordMax = Math.min(
                        (area.x + area.width) - 1,
                        run.getStop());

                    if (coordMax >= coordMin) {
                        count += (coordMax - coordMin + 1);
                    }
                }
            }
        }

        if (logger.isFineEnabled()) {
            logger.fine(
                "Stick" + getId() + " " + area + " getAlienPixelsIn=" + count);
        }

        return count;
    }

    //------------------//
    // getAliensAtStart //
    //------------------//
    /**
     * Count alien pixels in the following rectangle...
     * <pre>
     * +-------+
     * |       |
     * +=======+==================================+
     * |       |
     * +-------+
     * </pre>
     *
     * @param dCoord rectangle size along stick length
     * @param dPos   retangle size along stick thickness
     *
     * @return the number of alien pixels found
     */
    public int getAliensAtStart (int dCoord,
                                 int dPos)
    {
        return getAlienPixelsIn(
            new Rectangle(
                getStart(),
                getStartingPos() - dPos,
                dCoord,
                2 * dPos));
    }

    //-----------------------//
    // getAliensAtStartFirst //
    //-----------------------//
    /**
     * Count alien pixels in the following rectangle...
     * <pre>
     * +-------+
     * |       |
     * +=======+==================================+
     * </pre>
     *
     * @param dCoord rectangle size along stick length
     * @param dPos   retangle size along stick thickness
     *
     * @return the number of alien pixels found
     */
    public int getAliensAtStartFirst (int dCoord,
                                      int dPos)
    {
        return getAlienPixelsIn(
            new Rectangle(getStart(), getStartingPos() - dPos, dCoord, dPos));
    }

    //----------------------//
    // getAliensAtStartLast //
    //----------------------//
    /**
     * Count alien pixels in the following rectangle...
     * <pre>
     * +=======+==================================+
     * |       |
     * +-------+
     * </pre>
     *
     * @param dCoord rectangle size along stick length
     * @param dPos   retangle size along stick thickness
     *
     * @return the number of alien pixels found
     */
    public int getAliensAtStartLast (int dCoord,
                                     int dPos)
    {
        return getAlienPixelsIn(
            new Rectangle(getStart(), getStartingPos(), dCoord, dPos));
    }

    //-----------------//
    // getAliensAtStop //
    //-----------------//
    /**
     * Count alien pixels in the following rectangle...
     * <pre>
     *                                    +-------+
     *                                    |       |
     * +==================================+=======+
     *                                    |       |
     *                                    +-------+
     * </pre>
     *
     * @param dCoord rectangle size along stick length
     * @param dPos   retangle size along stick thickness
     *
     * @return the number of alien pixels found
     */
    public int getAliensAtStop (int dCoord,
                                int dPos)
    {
        return getAlienPixelsIn(
            new Rectangle(
                getStop() - dCoord,
                getStoppingPos() - dPos,
                dCoord,
                2 * dPos));
    }

    //----------------------//
    // getAliensAtStopFirst //
    //----------------------//
    /**
     * Count alien pixels in the following rectangle...
     * <pre>
     *                                    +-------+
     *                                    |       |
     * +==================================+=======+
     * </pre>
     *
     * @param dCoord rectangle size along stick length
     * @param dPos   retangle size along stick thickness
     *
     * @return the number of alien pixels found
     */
    public int getAliensAtStopFirst (int dCoord,
                                     int dPos)
    {
        return getAlienPixelsIn(
            new Rectangle(
                getStop() - dCoord,
                getStoppingPos() - dPos,
                dCoord,
                dPos));
    }

    //---------------------//
    // getAliensAtStopLast //
    //---------------------//
    /**
     * Count alien pixels in the following rectangle...
     * <pre>
     * +==================================+=======+
     *                                    |       |
     *                                    +-------+
     * </pre>
     *
     * @param dCoord rectangle size along stick length
     * @param dPos   retangle size along stick thickness
     *
     * @return the number of alien pixels found
     */
    public int getAliensAtStopLast (int dCoord,
                                    int dPos)
    {
        return getAlienPixelsIn(
            new Rectangle(getStop() - dCoord, getStoppingPos(), dCoord, dPos));
    }

    //-----------//
    // getAspect //
    //-----------//
    /**
     * Report the ratio of length over thickness
     *
     * @return the "slimness" of the stick
     */
    public double getAspect ()
    {
        return (double) getLength() / (double) getThickness();
    }

    //------------//
    // getDensity //
    //------------//
    /**
     * Report the density of the stick, that is its weight divided by the area
     * of its bounding rectangle
     *
     * @return the density
     */
    public double getDensity ()
    {
        Rectangle rect = getBounds();
        int       surface = (rect.width + 1) * (rect.height + 1);

        return (double) getWeight() / (double) surface;
    }

    //---------------//
    // isExtensionOf //
    //---------------//
    /**
     * Checks whether a provided stick can be considered as an extension of this
     * one.  Due to some missing points, a long stick can be broken into several
     * smaller ones, that we must check for this.  This is checked before
     * actually merging them.
     *
     * @param other           the other stick
     * @param maxDeltaCoord Max gap in coordinate (x for horizontal)
     * @param maxDeltaPos   Max gap in position (y for horizontal)
     * @param maxDeltaSlope Max difference in slope
     *
     * @return The result of the test
     */
    public boolean isExtensionOf (Stick  other,
                                  int    maxDeltaCoord,
                                  int    maxDeltaPos,
                                  double maxDeltaSlope)
    {
        // Check that a pair of start/stop is compatible
        if ((Math.abs(other.getStart() - getStop()) <= maxDeltaCoord) ||
            (Math.abs(other.getStop() - getStart()) <= maxDeltaCoord)) {
            // Check that a pair of positions is compatible
            if ((Math.abs(
                other.getLine().yAt(other.getStart()) -
                getLine().yAt(other.getStop())) <= maxDeltaPos) ||
                (Math.abs(
                other.getLine().yAt(other.getStop()) -
                getLine().yAt(other.getStart())) <= maxDeltaPos)) {
                // Check that slopes are compatible (a useless test ?)
                if (Math.abs(other.getLine().getSlope() - getLine().getSlope()) <= maxDeltaSlope) {
                    return true;
                } else if (logger.isFineEnabled()) {
                    logger.fine("isExtensionOf:  Incompatible slopes");
                }
            } else if (logger.isFineEnabled()) {
                logger.fine("isExtensionOf:  Incompatible positions");
            }
        } else if (logger.isFineEnabled()) {
            logger.fine("isExtensionOf:  Incompatible coordinates");
        }

        return false;
    }

    //-------------//
    // getFirstPos //
    //-------------//
    /**
     * Return the first position (ordinate for stick of horizontal sections,
     * abscissa for stick of vertical sections and runs)
     *
     * @return the position at the beginning
     */
    public int getFirstPos ()
    {
        return getBounds().y;
    }

    //---------------//
    // getFirstStuck //
    //---------------//
    /**
     * Compute the number of pixels stuck on first side of the stick
     *
     * @return the number of pixels
     */
    public int getFirstStuck ()
    {
        int stuck = 0;

        for (GlyphSection section : getMembers()) {
            Run sectionRun = section.getFirstRun();

            for (GlyphSection sct : section.getSources()) {
                if (!sct.isGlyphMember() || (sct.getGlyph() != this)) {
                    stuck += sectionRun.getCommonLength(sct.getLastRun());
                }
            }
        }

        return stuck;
    }

    //------------//
    // getLastPos //
    //------------//
    /**
     * Return the last position (maximum ordinate for a horizontal stick,
     * maximum abscissa for a vertical stick)
     *
     * @return the position at the end
     */
    public int getLastPos ()
    {
        return (getFirstPos() + getThickness()) - 1;
    }

    //--------------//
    // getLastStuck //
    //--------------//
    /**
     * Compute the nb of pixels stuck on last side of the stick
     *
     * @return the number of pixels
     */
    public int getLastStuck ()
    {
        int stuck = 0;

        for (GlyphSection section : getMembers()) {
            Run sectionRun = section.getLastRun();

            for (GlyphSection sct : section.getTargets()) {
                if (!sct.isGlyphMember() || (sct.getGlyph() != this)) {
                    stuck += sectionRun.getCommonLength(sct.getFirstRun());
                }
            }
        }

        return stuck;
    }

    //-----------//
    // getLength //
    //-----------//
    /**
     * Report the length of the stick
     *
     * @return the stick length in pixels
     */
    public int getLength ()
    {
        return getBounds().width;
    }

    //---------//
    // getLine //
    //---------//
    /**
     * Return the approximating line computed on the stick.
     *
     * @return The line
     */
    public Line getLine ()
    {
        if (line == null) {
            computeLine();
        }

        return line;
    }

    //-----------//
    // getMidPos //
    //-----------//
    /**
     * Return the position (ordinate for horizontal stick, abscissa for vertical
     * stick) at the middle of the stick
     *
     * @return the position of the middle of the stick
     */
    public int getMidPos ()
    {
        if (getLine()
                .isVertical()) {
            // Fall back value
            return (int) Math.rint((getFirstPos() + getLastPos()) / 2.0);
        } else {
            return (int) Math.rint(
                getLine().yAt((getStart() + getStop()) / 2.0));
        }
    }

    //----------//
    // getStart //
    //----------//
    /**
     * Return the beginning of the stick (xmin for horizontal, ymin for
     * vertical)
     *
     * @return The starting coordinate
     */
    public int getStart ()
    {
        return getBounds().x;
    }

    //---------------//
    // getStartPoint //
    //---------------//
    /**
     * Report the point at the beginning of the approximating line
     * @return the starting point of the stick line
     */
    public PixelPoint getStartPoint ()
    {
        Point start = lag.switchRef(
            new Point(getStart(), line.yAt(getStart())),
            null);

        return new PixelPoint(start.x, start.y);
    }

    //----------------//
    // getStartingPos //
    //----------------//
    /**
     * Return the best pos value at starting of the stick
     *
     * @return mean pos value at stick start
     */
    public int getStartingPos ()
    {
        if ((getThickness() >= 2) && !getLine()
                                          .isVertical()) {
            return getLine()
                       .yAt(getStart());
        } else {
            return getFirstPos() + (getThickness() / 2);
        }
    }

    //---------//
    // getStop //
    //---------//
    /**
     * Return the end of the stick (xmax for horizontal, ymax for vertical)
     *
     * @return The ending coordinate
     */
    public int getStop ()
    {
        return (getStart() + getLength()) - 1;
    }

    //--------------//
    // getStopPoint //
    //--------------//
    /**
     * Report the point at the end of the approximating line
     * @return the ending point of the line
     */
    public PixelPoint getStopPoint ()
    {
        Point stop = lag.switchRef(
            new Point(getStop(), line.yAt(getStop())),
            null);

        return new PixelPoint(stop.x, stop.y);
    }

    //----------------//
    // getStoppingPos //
    //----------------//
    /**
     * Return the best pos value at the stopping end of the stick
     *
     * @return mean pos value at stick stop
     */
    public int getStoppingPos ()
    {
        if ((getThickness() >= 2) && !getLine()
                                          .isVertical()) {
            return getLine()
                       .yAt(getStop());
        } else {
            return getFirstPos() + (getThickness() / 2);
        }
    }

    //--------------//
    // getThickness //
    //--------------//
    /**
     * Report the stick thickness
     *
     * @return the thickness in pixels
     */
    public int getThickness ()
    {
        return getBounds().height;
    }

    //------------------//
    // addGlyphSections //
    //------------------//
    /**
     * Add another glyph (with its sections of points) to this one
     *
     * @param other The merged glyph
     * @param linkSections Should we set the link from sections to glyph ?
     */
    @Override
    public void addGlyphSections (Glyph   other,
                                  boolean linkSections)
    {
        super.addGlyphSections(other, linkSections);
        line = null;
    }

    //------------//
    // addSection //
    //------------//
    /**
     * Add a section as a member of this stick.
     *
     * @param section The section to be included
     * @param link should the section point back to this stick?
     */
    public void addSection (StickSection section,
                            boolean      link)
    {
        super.addSection(section, /* link => */
                         true);

        // Include the section points
        getLine()
            .includeLine(section.getLine());
    }

    //----------//
    // colorize //
    //----------//
    /**
     * Set the display color of all sections that compose this stick.
     *
     * @param lag the containing lag
     * @param viewIndex index in the view list
     * @param color     color for the whole stick
     */
    public void colorize (Lag   lag,
                          int   viewIndex,
                          Color color)
    {
        if (lag == this.lag) {
            colorize(viewIndex, getMembers(), color);
        }
    }

    //----------//
    // colorize //
    //----------//
    /**
     * Set the display color of all sections gathered by the provided list
     *
     * @param viewIndex the proper view index
     * @param sections  the collection of sections
     * @param color     the display color
     */
    public void colorize (int                      viewIndex,
                          Collection<GlyphSection> sections,
                          Color                    color)
    {
        for (GlyphSection section : sections) {
            SectionView view = (SectionView) section.getView(viewIndex);
            view.setColor(color);
        }
    }

    //    //------------------//
    //    // computeDensities //
    //    //------------------//
    //    /**
    //     * Computes the densities around the stick mean line
    //     */
    //    public void computeDensities (int nWidth,
    //                                  int nHeight)
    //    {
    //        final int maxDist = 20; // TODO of course
    //
    //        // Allocate and initialize density histograms
    //        int[] histoLeft = new int[maxDist];
    //        int[] histoRight = new int[maxDist];
    //
    //        for (int i = maxDist - 1; i >= 0; i--) {
    //            histoLeft[i] = 0;
    //            histoRight[i] = 0;
    //        }
    //
    //        // Compute (horizontal) distances
    //        Line line = getLine();
    //
    //        for (GlyphSection section : getMembers()) {
    //            int pos = section.getFirstPos(); // Abscissa for vertical
    //
    //            for (Run run : section.getRuns()) {
    //                int stop = run.getStop();
    //
    //                for (int coord = run.getStart(); coord <= stop; coord++) {
    //                    int dist = (int) Math.rint(line.distanceOf(coord, pos));
    //
    //                    if ((dist < 0) && (dist > -maxDist)) {
    //                        histoRight[-dist] += 1;
    //                    }
    //
    //                    if ((dist >= 0) && (dist < maxDist)) {
    //                        histoLeft[dist] += 1;
    //                    }
    //                }
    //
    //                pos++;
    //            }
    //        }
    //
    //        System.out.println("computeDensities for Stick #" + id);
    //
    //        int     length = getLength();
    //        boolean started = false;
    //        boolean stopped = false;
    //
    //        for (int i = maxDist - 1; i >= 0; i--) {
    //            if (histoLeft[i] != 0) {
    //                started = true;
    //            }
    //
    //            if (started) {
    //                System.out.println(i + " : " + ((histoLeft[i] * 100) / length));
    //            }
    //        }
    //
    //        for (int i = -1; i > -maxDist; i--) {
    //            if (histoRight[-i] == 0) {
    //                stopped = true;
    //            }
    //
    //            if (!stopped) {
    //                System.out.println(
    //                    i + " : " + ((histoRight[-i] * 100) / length));
    //            }
    //        }
    //
    //        // Retrieve sections in the neighborhood
    //        Rectangle neighborhood = new Rectangle(getBounds());
    //        neighborhood.grow(nWidth, nHeight);
    //
    //        List<GlyphSection> neighbors = lag.getSectionsIn(neighborhood);
    //
    //        for (GlyphSection section : neighbors) {
    //            // Keep only sections that are not part of the stick
    //            if (section.getGlyph() != this) {
    //                System.out.println(section.toString());
    //            }
    //        }
    //    }

    //-------------//
    // computeLine //
    //-------------//
    /**
     * Computes the least-square fitted line among all the section points of the
     * stick.
     */
    public void computeLine ()
    {
        line = new BasicLine();

        for (GlyphSection section : getMembers()) {
            StickSection ss = (StickSection) section;
            line.includeLine(ss.getLine());
        }

        if (logger.isFineEnabled()) {
            logger.fine(
                line + " pointNb=" + line.getNumberOfPoints() +
                " meanDistance=" + (float) line.getMeanDistance());
        }
    }

    //------//
    // dump //
    //------//
    /**
     * Print out glyph internal data
     */
    @Override
    public void dump ()
    {
        super.dump();
        System.out.println("   line=" + getLine());
    }

    //------//
    // dump //
    //------//
    /**
     * Dump the stick as well as its contained sections is so desired
     *
     * @param withContent Flag to specify the dump of contained sections
     */
    public void dump (boolean withContent)
    {
        if (withContent) {
            System.out.println();
        }

        StringBuilder sb = new StringBuilder(toString());

        if (line != null) {
            sb.append(" pointNb=")
              .append(line.getNumberOfPoints());
        }

        sb.append(" start=")
          .append(getStart());
        sb.append(" stop=")
          .append(getStop());
        sb.append(" midPos=")
          .append(getMidPos());
        System.out.println(sb);

        if (withContent) {
            System.out.println("-members:" + getMembers().size());

            for (GlyphSection sct : getMembers()) {
                System.out.println(" " + sct.toString());
            }
        }
    }

    //-------------//
    // overlapWith //
    //-------------//
    /**
     * Check whether this stick overlaps with the other stick along their
     * orientation (that is abscissae for horizontal ones, and ordinates for
     * vertical ones)
     * @param other the other stick to check with
     * @return true if overlap, false otherwise
     */
    public boolean overlapWith (Stick other)
    {
        return Math.max(getStart(), other.getStart()) < Math.min(
            getStop(),
            other.getStop());
    }

    //------------//
    // renderLine //
    //------------//
    /**
     * Render the main guiding line of the stick, using the current foreground
     * color.
     *
     * @param g the graphic context
     */
    public void renderLine (Graphics g)
    {
        if (getContourBox()
                .intersects(g.getClipBounds())) {
            getLine(); // To make sure the line has been computed

            Point start = lag.switchRef(
                new Point(
                    getStart(),
                    (int) Math.rint(line.yAt((double) getStart()))),
                null);
            Point stop = lag.switchRef(
                new Point(
                    getStop() + 1,
                    (int) Math.rint(line.yAt((double) getStop() + 1))),
                null);
            g.drawLine(start.x, start.y, stop.x, stop.y);
        }
    }

    //----------//
    // toString //
    //----------//
    /**
     * A readable image of the Stick
     *
     * @return The image string
     */
    @Override
    public String toString ()
    {
        StringBuffer sb = new StringBuffer(256);
        sb.append(super.toString());

        if (getResult() != null) {
            sb.append(" ")
              .append(getResult());
        }

        if (!getMembers()
                 .isEmpty()) {
            sb.append(" th=")
              .append(getThickness());
            sb.append(" lg=")
              .append(getLength());
            sb.append(" l/t=")
              .append(String.format("%.2f", getAspect()));
            sb.append(" fa=")
              .append((100 * getFirstStuck()) / getLength())
              .append("%");
            sb.append(" la=")
              .append((100 * getLastStuck()) / getLength())
              .append("%");
        }

        if ((line != null) && (line.getNumberOfPoints() > 1)) {
            try {
                sb.append(" start[");

                PixelPoint start = getStartPoint();
                sb.append(start.x)
                  .append(",")
                  .append(start.y);
            } catch (Exception ignored) {
                sb.append("INVALID");
            } finally {
                sb.append("]");
            }

            try {
                sb.append(" stop[");

                PixelPoint stop = getStopPoint();
                sb.append(stop.x)
                  .append(",")
                  .append(stop.y);
            } catch (Exception ignored) {
                sb.append("INVALID");
            } finally {
                sb.append("]");
            }
        }

        if (this.getClass()
                .getName()
                .equals(Stick.class.getName())) {
            sb.append("}");
        }

        return sb.toString();
    }

    //-----------//
    // getPrefix //
    //-----------//
    /**
     * Return a distinctive string, to be used as a prefix in toString() for
     * example.
     *
     * @return the prefix string
     */
    @Override
    protected String getPrefix ()
    {
        return "Stick";
    }
}
