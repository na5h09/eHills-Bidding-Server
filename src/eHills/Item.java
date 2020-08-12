package eHills;

import java.io.Serializable;

class Item implements Serializable {
	  public String product;
	  public Double maxPrice;
	  public Double highestBid;
	  public String highestBidder;
	  public Boolean open;
//	  public Image pic;
	  
	  public Item(String product, Double price) {
		  this.product = product;
		  this.maxPrice = price;
		  this.highestBid = 5.00;
		  this.open = true;
	  }
	  
	  public void updateBid(Double newBid, String bidder) {
		  this.highestBid = newBid;
		  this.highestBidder = bidder;
	  }
	  
	  public Boolean validBid(Double bidPrice, String name) {
		  if(bidPrice <= this.highestBid) {
			  return false;
		  }
		  
		  if(bidPrice >= this.maxPrice) {
			  this.updateBid(bidPrice, name);
			  this.open = false;
		  } else {
			  this.updateBid(bidPrice, name);
		  }
		  
		  return true;
	  }
}
