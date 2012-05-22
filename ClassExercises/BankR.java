package Accounts;

import java.util.Random;

/**
 *
 */
public class BankR implements Runnable
{
    private static int numThreads = 4; //4 threads
    private static int numTranx = 1000;  //transactions *per thread*
    private static int amountEachTransfer = 2; //amount of every transaction

    private static Account a = new Account(10);
    private static Account b = new Account(10);
    private static Account c = new Account(10);
    private static Account d = new Account(10);

    //Transfers to use in testing
    //options
    final static int ONLY_A_TO_B = 1;
    final static int A_AND_B_ONLY = 2;
    final static int ALL_POSSIBILITIES = 12;

    //static int allowed_transfers = ONLY_A_TO_B;
	static int allowed_transfers = A_AND_B_ONLY;
	//static int allowed_transfers = ALL_POSSIBILITIES;


    public static void main(String[] args)
    {
        try
        {
            //hold all the transaction threads
            Thread[] threads = new Thread[numThreads];

            //create and start all the threads
            for (int i =0; i < numThreads; i++)
            {
                threads[i] = new Thread(new BankR());
                threads[i].start();
            }

            //wait for all threads to end
            for (int j =0; j < numThreads; j++)
            { threads[j].join(); }
        }
        catch (InterruptedException e)
        { e.printStackTrace(); } //To change body of catch statement use File | Settings | File Templates.
        finally
        {
            System.out.println("Final balance for A = " + a.getBalance());
            System.out.println("Final balance for B = " + b.getBalance());
            if (allowed_transfers == ALL_POSSIBILITIES)
            {
                System.out.println("Final balance for C = " + c.getBalance());
                System.out.println("Final balance for D = " + d.getBalance());
            }
        }
    }
    /**
     * What the thread does:
     *  Makes numTranx transfers from a to b
     */
    public void run()
    {
        Random r = new Random();
        for (int t = 0; t < numTranx; t++)
        {

            int which = r.nextInt(allowed_transfers);

            if (which == 0)
            { a.transfer(amountEachTransfer, b); }

            else if (which == 1)
            { b.transfer(amountEachTransfer, a); }

            else if (which == 2)
            { a.transfer(amountEachTransfer, c); }
            else if (which == 3)
            { a.transfer(amountEachTransfer, d); }
            else if (which == 4)
            { b.transfer(amountEachTransfer, c); }
            else if (which == 5)
            { b.transfer(amountEachTransfer, d); }
            else if (which == 6)
            { c.transfer(amountEachTransfer, a); }
            else if (which == 7)
            { c.transfer(amountEachTransfer, b); }
            else if (which == 8)
            { c.transfer(amountEachTransfer, d); }
            else if (which == 9)
            { d.transfer(amountEachTransfer, a); }
            else if (which == 10)
            { d.transfer(amountEachTransfer, c); }
            else if (which == 11)
            { d.transfer(amountEachTransfer, b); }
        }

    }
}
