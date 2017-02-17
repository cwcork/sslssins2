// **********************************************************************
//
// Copyright (c) 2003-2011 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************
package cxro.common.device.axis;

import cxro.common.device.IOError;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client extends Ice.Application
{
    @Override
    public int
    run(String[] args)
    {
        if(args.length > 0)
        {
            System.err.println(appName() + ": too many arguments");
            return 1;
        }

        AxisIcePrx axis = AxisIcePrxHelper.checkedCast(
            communicator().propertyToProxy("Axis.Proxy").ice_timeout(-1).ice_secure(false));

        if(axis == null)
        {
            System.err.println("invalid proxy");
            return 1;
        }

        try
        {
            System.out.println("Test Axis interface");

            // enable/disable axis
            System.out.format("\tisEnabled?  status  = %b%n",axis.isEnabled());
            System.out.println("\tdisable Axis");
            axis.disable();
            System.out.format("\tisEnabled?  status  = %b%n",axis.isEnabled());
            System.out.println("\tenable Axis");
            axis.enable();
            System.out.format("\tisEnabled?  status  = %b%n",axis.isEnabled());
            System.out.println("");

            // initialize axis
            System.out.format("\tisInitialized? status = %b%n",axis.isInitialized());
            System.out.println("\tinitialize");
            axis.initialize();
            System.out.format("\tisInitialized? status = %b%n",axis.isInitialized());
            System.out.println("");

           // Get/Set position and offset
            double rpos;
            double pos;
            System.out.println("Get current position");
            rpos = axis.getPositionRaw();
            pos  = axis.getPosition();
            System.out.format("raw    pos: %#f %n", rpos);
            System.out.format("cooked pos: %#f %n", pos);

            System.out.println("setPosition(pos + 1.0)");
            axis.setPosition(pos + 1.0);
            System.out.format("raw    pos: %#f %n", axis.getPositionRaw());
            System.out.format("cooked pos: %#f %n", axis.getPosition());

            System.out.println("Restore offset and positions");
            axis.setPosition(pos);
            System.out.format("raw    pos: %#f %n", axis.getPositionRaw());
            System.out.format("cooked pos: %#f %n", axis.getPosition());


            // Do some moves
            int status;
            boolean isReady;
            System.out.println("MAKE SURE WE ARE READY TO MOVE");
            isReady = axis.isReady();
            System.out.format("isReady? = %b%n", isReady);
            while (!isReady)
            {
                Thread.sleep(100);
                isReady = axis.isReady();
            }

            System.out.print("MOVE TO +3.0 mm ABSOLUTE ...");
            status = axis.moveAbsolute(3.0);
            if (status != 0)
            {
                System.out.println("\n  moveAbsolute failed, status = " + status);
                System.exit(1);
            }
            else
            {
                do
                {
//                    Thread.sleep(100);
                    isReady = axis.isReady();
                }
                while (!isReady);
            }
            System.out.println(" DONE.");
            System.out.format("PositionRaw: %#f %n", axis.getPositionRaw());
            System.out.format("Position   : %#f %n", axis.getPosition());
            System.out.println("");

            System.out.println("MOVE RELATIVE BY -3.0 mm ... ");
            status  = axis.moveRelative(-3.0);
            if (status != 0)
            {
                System.out.println("\n  moveRelative failed, status = " + status);
            }
            else
            {
                do
                {
//                    Thread.sleep(100);
                    isReady = axis.isReady();
                }
                while (!isReady);
            }
            System.out.println(" DONE.");
            System.out.format("PositionRaw: %#f %n", axis.getPositionRaw());
            System.out.format("Position   : %#f %n", axis.getPosition());
            System.out.println("");

            // abortMove
            System.out.println("\tabortMove()");
            try
            {
                axis.abortMove();
            }
            catch (IOError ex)
            {
                System.out.println("\tEXPECTED: IOError");
            }

            // done
            System.out.println("DONE");
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOError ex)
        {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (Ice.LocalException ex)
        {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    public static void
    main(String[] args)
    {
        Client app = new Client();
        int status = app.main("Client", args, "config.client");
        System.exit(status);
    }
}

