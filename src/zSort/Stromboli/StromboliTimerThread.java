/*
 *  StromboliTimerThread.java
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
 *  Created by Andreas Schweizer on Sat Nov 30 2002.
 *  Copyright (c) 2002 Andreas Schweizer.
 *
 */

// call x.start(), x.stop() to control thread execution

public class StromboliTimerThread extends java.lang.Thread
{
    private StromboliTimerListener tlis;
    private long intervalMillis;
    
    public StromboliTimerThread(StromboliTimerListener lis, long intervalMillis)
    {
        this.tlis = lis;
        this.intervalMillis = intervalMillis;
    }
    
    public void run()
    {
        do {
            tlis.tick();
            try {
                sleep(intervalMillis);
            } catch (Exception e) { }
        } while (true);
    }
}
