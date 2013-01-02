/*
 *  StromboliCurvesMessage.java
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
 *  Created by Andreas Schweizer on Wed Dec 18 2002.
 *  Copyright (c) 2002 Andreas Schweizer.
 *
 */

// Messages that the 'observable' StromboliCurves sends to the listeners.

public class StromboliCurvesMessage
{
    public final static int REALTIME_TICK = 1;
    public final static int CURVE_ADDED = 2; 
    public final static int CURVE_REMOVED = 3; 
    public final static int ALL_CURVES_REMOVED = 4;
    public final static int SELECTION_CHANGED = 5;
    
    private int msg;
    
    public StromboliCurvesMessage(int m)
    {
        msg = m;
    }
    
    public int msg()
    {
        return msg;
    }
}
