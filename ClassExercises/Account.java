package Accounts;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 *
 */
public class Account
{
	
	Lock lock = new ReentrantLock();
	
    public Account(float balance)
    {
        this.balance = balance;
    }


	public boolean tryObtainLocks(Account to) {
		Boolean myLock = false;
		Boolean hisLock = false;	
		
		try {
			myLock = this.lock.tryLock();
			hisLock = to.lock.tryLock();
		} finally {
			if (! (myLock && hisLock)) {
				
				
                if (myLock) {
                   this.lock.unlock();
               }
               if (hisLock) {
                   to.lock.unlock();
               }
           	}
		}
		
		return myLock && hisLock;
	}


    /**
     * This versions requires that the balance is never negative.  This is sensible in that
     * Amy should never be allowed to transfer more money to Betty than Amy actually has*.
     * If transfers can interleave, then two transfers from Amy can both pass the if statement
     * even if there is only enough money for one of them.  Thus transfer will need to be
     * synchronized.  However, as demonstrated in class, this can cause a deadlock.
     *
     * *Note the following race condition:
     *    Amy has $0.  Carol is giving $10 to Amy (t1).  Amy is giving $10 to Betty (t2).
     *    If t2 happens to be scheduled before t1, then t2 will fail. If t1 is scheduled before
     *    t2, then both succeed.  
     *
     */
    public void transfer(float amount, Account to)
    {
		boolean transactionDone = false;
	
		while (true) {
			//waitTime(2000);
		
			if (tryObtainLocks(to)) {
				//obtained locks
				try {
					//perform transactions
					if (amount < balance)
			        {
			                Thread.yield(); //this just makes the multithreading issues more common
			                this.debit(amount);
			                to.credit(amount);
			         }
			        if (balance < 0)
			        {
			            System.out.println("Negative balance detected: balance = " + balance);
			        }
		
					transactionDone = true;
				
				} finally {
					this.lock.unlock();
					to.lock.unlock();
				}
			
			} 
			
			if (!transactionDone) {
				continue;
			} else {
				break;
			}
			
		}//while
	
	
        
    }

    /**
     * to make a problem more likely, add either of the lines below in between 1 & 2:
     *      Thread.yield();
     *      System.out.println("got balance: " + balance);
     *
     * to make problems less likely try with just:
     *      balance -= amount;
     * then run a few times.  you (may) see that though less common, problems still happen!
     */
    private void debit (float amount)
    {
        float balance = getBalance();   //1
        balance = balance - amount;     //2
    	saveBalance(balance);           //3
    }

    private void credit(float amount)
    {
		
        	float balance = getBalance();
	        balance = balance + amount;
	    	saveBalance(balance);
		
    }

	public void waitTime(long millisecond){  
	        long max = millisecond;  
	        for(long i = 0;  i < max; i++){  
	            for(long j = 0;  j < max; j++){  

	            }  
	        }  
	    }


//------------------------------------------------------------    
    private float balance;

    public float getBalance()
    { return this.balance; }

    /**
     * saves balance to storage, in this case just "this.balance", but in practice could
     * be a DB, another system, a file, ...
     */
    private void saveBalance(float amount)
    { this.balance = amount; }




}
