/**
 * MIT License (MIT)
 *
 * Copyright (c) 2014 Volker Berlin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * UT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author Volker Berlin
 * @license: The MIT license <http://opensource.org/licenses/MIT>
 */
package com.inet.lib.less;


/**
 * Some methods for calculating colors.
 */
class ColorUtils {

    static HSL toHSL( double color ) {
        long argb = Double.doubleToRawLongBits( color );
        double a = alpha( color );
        double r = clamp( ((argb >> 32) & 0xFFFF) / (double)0xFF00 );
        double g = clamp( ((argb >> 16) & 0xFFFF) / (double)0xFF00 );
        double b = clamp( ((argb >> 0) & 0xFFFF) / (double)0xFF00 );

        double max = Math.max( Math.max( r, g ), b );
        double min = Math.min( Math.min( r, g ), b );
        double h, s, l = (max + min) / 2, d = max - min;

        if( max == min ) {
            h = s = 0;
        } else {
            s = l > 0.5 ? d / (2 - max - min) : d / (max + min);

            if( max == r ) {
                h = (g - b) / d + (g < b ? 6 : 0);
            } else if( max == g ) {
                h = (b - r) / d + 2;
            } else {
                h = (r - g) / d + 4;
            }
            h /= 6;
        }
        return new HSL( h * 360, s, l, a );
    }

    static double clamp( double val ) {
        return Math.min(1, Math.max(0, val));
    }

    static double rgba( double r, double g, double b, double a ) {
        return Double.longBitsToDouble( Math.round( a * 0xFFFF ) << 48 | (colorLargeDigit(r) << 32) | (colorLargeDigit(g) << 16) | colorLargeDigit(b) );
    }

    static double rgba( int r, int g, int b, double a ) {
        return Double.longBitsToDouble( Math.round( a * 0xFFFF ) << 48 | (colorLargeDigit(r) << 32) | (colorLargeDigit(g) << 16) | colorLargeDigit(b) );
    }

    static double rgb( int r, int g, int b ) {
        return Double.longBitsToDouble( Expression.ALPHA_1 | (colorLargeDigit(r) << 32) | (colorLargeDigit(g) << 16) | colorLargeDigit(b) );
    }

    static int argb( double color ) {
        long value = Double.doubleToRawLongBits( color );
        int result = colorDigit( ((value >>> 48)) / 256.0 ) << 24;
        result |= colorDigit( ((value >> 32) & 0xFFFF) / 256.0 ) << 16; 
        result |= colorDigit( ((value >> 16) & 0xFFFF) / 256.0 ) << 8; 
        result |= colorDigit( ((value) & 0xFFFF) / 256.0 ); 
        return result;
    }

    static double alpha( double color ) {
        double value = (Double.doubleToRawLongBits( color ) >>> 48) / (double)0XFFFF;
        return Math.round( value * 10000 ) / 10000.0;
    }

    static int red( double color ) {
        return colorDigit( ((Double.doubleToRawLongBits( color ) >> 32) & 0xFFFF) / 256.0 ); 
    }

    static int green( double color ) {
        return colorDigit( ((Double.doubleToRawLongBits( color ) >> 16 & 0xFFFF)) / 256.0 ); 
    }

    static int blue( double color ) {
        return colorDigit( (Double.doubleToRawLongBits( color ) & 0xFFFF) / 256.0 );
    }

    private static double hsla_hue(double h, double m1, double m2) {
        h = h < 0 ? h + 1 : (h > 1 ? h - 1 : h);
        if      (h * 6 < 1) { return m1 + (m2 - m1) * h * 6; }
        else if (h * 2 < 1) { return m2; }
        else if (h * 3 < 2) { return m1 + (m2 - m1) * (2F/3 - h) * 6; }
        else                { return m1; }
    }

    static double hsla (HSL hsl) {
        return hsla(hsl.h, hsl.s, hsl.l, hsl.a);
    }

    static double hsla( double h, double s, double l, double a ) {

        h = (h % 360) / 360;
        s = clamp(s); l = clamp(l); a = clamp(a);

        double m2 = l <= 0.5 ? l * (s + 1) : l + s - l * s;
        double m1 = l * 2 - m2;

        return rgba( hsla_hue(h + 1.0/3, m1, m2) * 255,
                     hsla_hue(h        , m1, m2) * 255,
                     hsla_hue(h - 1.0/3, m1, m2) * 255,
                     a);
    }

    static double luminance( double color ) {
        long argb = Double.doubleToRawLongBits( color );
        double r = ((argb >> 32) & 0xFFFF) / (double)0xFF00;
        double g = ((argb >> 16) & 0xFFFF) / (double)0xFF00;
        double b = ((argb) & 0xFFFF) / (double)0xFF00;
        return (0.2126 * r) + (0.7152 * g) + (0.0722 * b);
    }

    static double luma( double color ) {
        long argb = Double.doubleToRawLongBits( color );
        double r = ((argb >> 32) & 0xFFFF) / (double)0xFF00;
        double g = ((argb >> 16) & 0xFFFF) / (double)0xFF00;
        double b = ((argb) & 0xFFFF) / (double)0xFF00;

        r = (r <= 0.03928) ? r / 12.92 : Math.pow( ((r + 0.055) / 1.055), 2.4 );
        g = (g <= 0.03928) ? g / 12.92 : Math.pow( ((g + 0.055) / 1.055), 2.4 );
        b = (b <= 0.03928) ? b / 12.92 : Math.pow( ((b + 0.055) / 1.055), 2.4 );

        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    static double contrast( double color, double dark, double light, double threshold ) {
        //Figure out which is actually light and dark!
        if( luma( dark ) > luma( light ) ) {
            double t = light;
            light = dark;
            dark = t;
        }
        if( luma( color ) < threshold ) {
            return light;
        } else {
            return dark;
        }
    }

    static HSV toHSV( double color ) {
        long argb = Double.doubleToRawLongBits( color );
        double a = alpha( color );
        double r = clamp( ((argb >> 32) & 0xFFFF) / (double)0xFF00 );
        double g = clamp( ((argb >> 16) & 0xFFFF) / (double)0xFF00 );
        double b = clamp( ((argb >> 0) & 0xFFFF) / (double)0xFF00 );

        double max = Math.max(Math.max(r, g), b);
        double min = Math.min(Math.min(r, g), b);
        double h, s, v = max;

        double d = max - min;
        if (max == 0) {
            s = 0;
        } else {
            s = d / max;
        }

        if (max == min) {
            h = 0;
        } else if( max == r ){
            h = (g - b) / d + (g < b ? 6 : 0);
        } else if( max == g ){
            h = (b - r) / d + 2;
        } else { //if( max == b ){
            h = (r - g) / d + 4;
        }
        h /= 6;
        return new HSV( h * 360, s, v,  a );
    }

    private static final int[][] HSVA_PERM = { { 0, 3, 1 }, //
                    { 2, 0, 1 }, //
                    { 1, 0, 3 }, //
                    { 1, 2, 0 }, //
                    { 3, 1, 0 }, //
                    { 0, 1, 2 }           }; //

    static double hsva( double hue, double saturation, double value, double alpha ) {
        hue = ((hue % 360) / 360) * 360;

        int i = (int)Math.floor( (hue / 60) % 6 );
        double f = (hue / 60) - i;

        double[] vs = { value, value * (1 - saturation), value * (1 - f * saturation), value * (1 - (1 - f) * saturation) };

        return rgba( vs[HSVA_PERM[i][0]] * 255, vs[HSVA_PERM[i][1]] * 255, vs[HSVA_PERM[i][2]] * 255, alpha );
    }

    /**
     * Calculate the mix color of 2 colors.
     * @param color1
     * @param color2
     * @param weight balance point between the two colors in range of 0 to 1. 
     * @return the resulting color
     */
    static double mix( double color1, double color2, double weight ) {
        long col1 = Double.doubleToRawLongBits( color1 );
        long col2 = Double.doubleToRawLongBits( color2 );

        int alpha1 = (int)(col1  >>> 48);
        int red1 = (int)(col1  >> 32) & 0xFFFF;
        int green1 = (int)(col1  >> 16) & 0xFFFF;
        int blue1 = (int)(col1) & 0xFFFF;
        int alpha2 = (int)(col2  >>> 48);
        int red2 = (int)(col2  >> 32) & 0xFFFF;
        int green2 = (int)(col2  >> 16) & 0xFFFF;
        int blue2 = (int)(col2) & 0xFFFF;

        double w = weight * 2 - 1;
        double a = (alpha1 - alpha2) / (double)0XFFFF;

        double w1 = (((w * a == -1) ? w : (w + a) / (1 + w * a)) + 1) / 2.0;
        double w2 = 1 - w1;

        long red = Math.round(red1 * w1 + red2 * w2);
        long green = Math.round(green1 * w1 + green2 * w2);
        long blue = Math.round(blue1 * w1 + blue2 * w2);

        long alpha = Math.round(alpha1 * weight + alpha2 * (1 - weight));

        long color = (alpha << 48) | (red << 32) | (green << 16) | (blue);
        return Double.longBitsToDouble( color );
    }

    static double multiply( double color1, double color2 ) {
        long argb1 = Double.doubleToRawLongBits( color1 );
        long r1 = ((argb1 >> 32) & 0xFFFF);
        long g1 = ((argb1 >> 16) & 0xFFFF);
        long b1 = ((argb1) & 0xFFFF);

        long argb2 = Double.doubleToRawLongBits(color2);
        long r2 = ((argb2 >> 32) & 0xFFFF);
        long g2 = ((argb2 >> 16) & 0xFFFF);
        long b2 = ((argb2) & 0xFFFF);

        argb1 = ((r1 * r2) / 0xFF00) << 32 | ((g1 * g2) / 0xFF00) << 16 | ((b1 * b2) / 0xFF00);

        return Double.longBitsToDouble( argb1 );
    }

    static double screen( double color1, double color2 ) {
        long argb1 = Double.doubleToRawLongBits( color1 );
        long r1 = ((argb1 >> 32) & 0xFFFF);
        long g1 = ((argb1 >> 16) & 0xFFFF);
        long b1 = ((argb1) & 0xFFFF);

        long argb2 = Double.doubleToRawLongBits(color2);
        long r2 = ((argb2 >> 32) & 0xFFFF);
        long g2 = ((argb2 >> 16) & 0xFFFF);
        long b2 = ((argb2) & 0xFFFF);

        argb1 = (r1 + r2 - ((r1 * r2) / 0xFF00)) << 32 | (g1 + g2 - ((g1 * g2) / 0xFF00)) << 16 | (b1 + b2 - ((b1 * b2) / 0xFF00));

        return Double.longBitsToDouble( argb1 );
    }

    static int colorDigit( double value ) {
        if( value >= 255 ) {
            return 255;
        } else if( value <= 0 ) {
            return 0;
        } else {
            return (int)Math.round( value );
        }
    }

    private static long colorLargeDigit( double value ) {
        value *= 0x100;
        if( value >= 0xFFFF ) {
            return 0xFFFF;
        } else if( value <= 0 ) {
            return 0;
        } else {
            return Math.round( value );
        }
    }

    /**
     * Get the expression value as percent (range 0 - 1).
     * 
     * @param expression
     *            the expression
     * @return the percent value
     */
    static double getPercent( Expression expression, CssFormatter formatter ) {
        double d = expression.doubleValue( formatter );
        if( expression.getDataType( formatter ) == Expression.PERCENT ) {
            d /= 100;
        }
        return d;
    }
}
