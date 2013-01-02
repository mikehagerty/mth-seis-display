/*
 *  StromboliTerrain.java
 *  This file is part of StromboliApplet.
 *  
 *  StromboliApplet is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  StromboliApplet is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with StromboliApplet; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Created by Andreas Schweizer on Sat Nov 23 2002.
 *  Copyright (c) 2002 Andreas Schweizer.
 *
 */

import java.util.Vector;

public class StromboliTerrain
{
    private static StromboliTerrain gActiveTerrain;
    private StromboliTerrainData td;
    private int kMaxTPs = 256;
    
    public StromboliTerrain(StromboliTerrainData td)
    {
        gActiveTerrain = this;
        this.td = td;
    }
    
    public static double _heightApprox(double px, double py)
    {
        return gActiveTerrain.heightApprox(px, py);
    }
    
    public Vector crossSection(double azDeg, double stepWidth)
    {
        Vector mp;
        double tx, ty, tz;
        double dst;
        int i;
        
        mp = new Vector();
        
        for (i=0; i<kMaxTPs; i++) {
            dst = i * stepWidth;
            tx = dst * java.lang.Math.sin(azDeg / 180.0 * java.lang.Math.PI);
            ty = java.lang.Math.sqrt(dst*dst - tx*tx);
            if ((azDeg > 90.0) && (azDeg < 270.0)) ty = -ty;
            tz = heightApprox(tx, ty);
            mp.addElement(new StromboliPoint(dst, tz));
        }
        
        return mp;
    }

    public double heightApprox(double px, double py)
    {
        double pz = 0.0;		// result
        double qx, qy, qz, rx, ry, rz, sx, sy, sz;
        double nx = 0, ny = 0, nz = 0;
        double n1, n2;
        double minDist, quadDist;
        double a;
        boolean gFlag = false, hFlag;
        int i;
        
	minDist = +1E20;
        for (i=0; i<td.tp.length; i+=3) {
            qx = td.tp[i];
            qy = td.tp[i+1];
            n1 = (qx - px);
            n2 = (qy - py);
            quadDist = n1 * n1 + n2 * n2;
            if (quadDist < minDist) {
                minDist = quadDist;
                qz = td.tp[i+2];
                nx = qx; ny = qy; nz = qz;
            }
        }
        qx = nx; qy = ny; qz = nz;
        
        if (minDist == 0) {
            // P liegt gerade auf Q, also sind wir fertig
            pz = qz;
            return pz;
        
        } else {
            // Wir brauchen einen 2. und 3. Punkt
            a = (py - qy) / (px - qx);
            hFlag = (py + (px - qx) / a) > qy;
            minDist = +1E20;
            
            for (i=0; i<td.tp.length; i+=3) {
                rx = td.tp[i];
                ry = td.tp[i+1];
                n1 = (rx - px);
                n2 = (ry - py);
		quadDist = n1 * n1 + n2 * n2;
                if (quadDist<minDist) {
                    if (((py + (px-rx)/a) > ry) != hFlag) {
                        minDist = quadDist;
                        gFlag = (py + a*(rx-px)) > ry;
                        rz = td.tp[i+2];
                        nx = rx; ny = ry; nz = rz;
                    }
                }
            }
            rx = nx; ry = ny; rz = nz;
            if (minDist == +1E20) {
                // Keinen 2. Punkt gefunden, also mit erstem Vorlieb nehmen
                pz = qz;
                return pz;
            } else {
                // R gefunden, also S suchen
                minDist = +1E20;
                for (i=0; i<td.tp.length; i+=3) {
                    sx = td.tp[i];
                    sy = td.tp[i+1];
                    n1 = (sx - px);
                    n2 = (sy - py);
                    quadDist = n1 * n1 + n2 * n2;
                    if (quadDist < minDist) {
                        if (((py + (px-sx)/a) > sy) != hFlag) {
                            if (((py + a*(sx - px)) > sy) != gFlag) {
                                minDist = quadDist;
                                sz = td.tp[i+2];
                                nx = sx; ny = sy; nz = sz;
                            }
                        }
                    }
                }
                sx = nx; sy = ny; sz = nz;
                if (minDist == +1E20) {
                    // Keinen 3. Punkt gefunden, also mit erstem Vorlieb nehmen
                    pz = qz;
                    return pz;
                } else {
                    nx = (sy - qy) * (rz - qz) - (sz - qz) * (ry - qy);
                    ny = (sz - qz) * (rx - qx) - (sx - qx) * (rz - qz);
                    nz = (sx - qx) * (ry - qy) - (sy - qy) * (rx - qx);
                    pz = (nx * (qx - px) + ny * (qy - py) + nz * qz) / nz;
                }
            }
        }
        return pz;
    }
}
