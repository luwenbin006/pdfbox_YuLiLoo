/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.graphics.image;

import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

/**
 * An image factory.
 *
 * @author John Hewson
 * @author Brigitte Mathiak
 */
class ImageFactory
{
    protected ImageFactory()
    {
    }

    // returns a PDColorSpace for a given BufferedImage
    protected static PDColorSpace getColorSpaceFromAWT(BufferedImage awtImage)
    {
        if (awtImage.getColorModel().getNumComponents() == 1)
        {
            // 256 color (gray) JPEG
            return PDDeviceGray.INSTANCE;
        }
        else
        {
            return toPDColorSpace(awtImage.getColorModel().getColorSpace());
        }
    }

    // returns a PDColorSpace for a given AWT ColorSpace
    protected static PDColorSpace toPDColorSpace(ColorSpace awtColorSpace)
    {
        if (awtColorSpace instanceof ICC_ColorSpace && !awtColorSpace.isCS_sRGB())
        {
            throw new UnsupportedOperationException("ICC color spaces not implemented");
        }
        else
        {
            switch (awtColorSpace.getType())
            {
                case ColorSpace.TYPE_RGB:  return PDDeviceRGB.INSTANCE;
                case ColorSpace.TYPE_GRAY: return PDDeviceGray.INSTANCE;
                case ColorSpace.TYPE_CMYK: return PDDeviceCMYK.INSTANCE;
                default: throw new UnsupportedOperationException("color space not implemented: " +
                        awtColorSpace.getType());
            }
        }
    }

    // returns the color channels of an image
    protected static BufferedImage getColorImage(BufferedImage image)
    {
        if (!image.getColorModel().hasAlpha())
        {
            return image;
        }

        if (image.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB)
        {
            throw new UnsupportedOperationException("only RGB color spaces are implemented");
        }

        // create an RGB image without alpha
        //BEWARE: the previous solution in the history 
        // g.setComposite(AlphaComposite.Src) and g.drawImage()
        // didn't work properly for TYPE_4BYTE_ABGR.
        // alpha values of 0 result in a black dest pixel!!!
        BufferedImage rgbImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        return new ColorConvertOp(null).filter(image, rgbImage);
    }
}
