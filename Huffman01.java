import java.util.*;
import java.io.*;

public class Huffman01{
    
  public static void main(String[] args){
    
    //The following data structure is used to
    // communicate encoding particulars from the Huffman
    // encoder to the Huffman decoder.  This is necessary
    // for the decoder to be able to decode the encoded
    // message.  Note that this data structure must be
    // empty when it is passed to the encode method.
    Hashtable <Character,String>huffEncodeTable;
    
    //Begin the demonstration by applying Huffman encoding
    // to a text message.
                        
    //Create and display the raw text message that will be
    // encoded.  Display 48 characters to the line.
    
    //Modify the comment indicators to enable one of the
    // following test messages, or insert a test message
    // of your own and then recompile the program.
/*
    //The following test message was copied directly from
    // an Internet news site.  It is probably
    // representative of typical English text.
    String rawData = "BAGHDAD, Iraq Violence increased "
    + "across Iraq after a lull following the Dec. 15 "
    + "parliamentary elections, with at least two dozen "
    + "people including a U.S. soldier killed Monday in "
    + "shootings and bombings mostly targeting the Shiite-"
    + "dominated security services. The Defense Ministry "
    + "director of operations, Brig. Gen. Abdul Aziz "
    + "Mohammed-Jassim, blamed increased violence in the "
    + "past two days on insurgents trying to deepen the "
    + "political turmoil following the elections. The "
    + "violence came as three Iraqi opposition groups "
    + "threatened another wave of protests and civil "
    + "disobedience if allegations of fraud are not "
    + "properly investigated.";
*/

    //Use the following test message or some other 
    // similarly short test message to illustrate the
    // construction of the HuffTree object.

    String text = "";
    try {
        Scanner scanner = new Scanner(new File("input.txt"));
        text = scanner.useDelimiter("\\A").next();
    } catch (FileNotFoundException e) {
        System.err.println("File not found!");
    }
    String rawData = text;
    
    //Enable the following two statements to display the
    // raw data 48 characters to the line.
    System.out.println("Raw Data: '" + rawData + "'");
    
    //DISPLAY RAW INPUT DATA
    System.out.println("\nUncompressed Bit Data: ");
    for(int cnt = 0,charCnt = 0;cnt < rawData.length();
                                          cnt++,charCnt++){
      char theCharacter = rawData.charAt(cnt);
      String binaryString = Integer.toBinaryString(theCharacter);
      //Append leading zeros as necessary to show eight
      // bits per character.
      while(binaryString.length() < 8){
        binaryString = "0" + binaryString;
      }//end while loop
      if(charCnt%6 == 0){
        //Display 48 bits per line.
        charCnt = 0;
      }//end if
      System.out.print(binaryString);
    }//end for loop
    System.out.println();
    System.out.println("Size Before Compression: " + rawData.length() * 8 + " bits");

    int rawDataLen = rawData.length();

    //Instantiate a Huffman encoder object
    HuffmanEncoder encoder = new HuffmanEncoder();
    
    //Encode the raw text message.  The encoded message
    // is received back as bytes stored in an ArrayList
    // object.  Pass the raw message to the encode
    // method.  Also pass a reference to the empty data
    // structure mentioned above to the encode method where
    // it will be populated with encoding particulars
    // needed to decode the message later
    huffEncodeTable = new Hashtable<Character,String>();
    ArrayList<Byte> binaryEncodedData = encoder.encode(rawData,huffEncodeTable);
    
    //The message has now been Huffman encoded. Display the
    // binaryEncodedData in Hexadecimal format, 48 
    // characters per line.
    /*
    System.out.println("nBinary Encoded Data in Hexadecimal Format");
    hexDisplay48(binaryEncodedData);
    */
    
    //Now continue the demonstration by decoding the
    // Huffman-encoded message.

    //Instantiate a Huffman decoder object.
    HuffmanDecoder decoder = new HuffmanDecoder();
    
    //Pass the encoded message to the decode method of the
    // HuffmanDecoder object.  Also pass a reference
    // to the  data structure containing encoding 
    // particulars to the decode method.  Also pass the
    // length of the original message so that extraneous
    // characters on the end of the decoded message can be
    // eliminated.
    String decodedData = decoder.decode(binaryEncodedData,huffEncodeTable,rawDataLen);
    //Display compression factor
    double rawFactor = (double)rawData.length()/binaryEncodedData.size();
    float cFactor = (float)Math.round(rawFactor);
    System.out.println("\nCompression factor: ~" + cFactor);
    //Display the decoded results
    System.out.println("\nDecoded Data: '" + decodedData + "'");

  }//end main
  //-----------------------------------------------------//
  
  //Utility method to display a String 48 characters to
  // the line.
  static void display48(String data){
    for(int cnt = 0;cnt < data.length();cnt += 48){
      if((cnt + 48) < data.length()){
        //Display 48 characters.
        System.out.println(data.substring(cnt,cnt+48));
      }else{
        //Display the final line, which may be short.
        System.out.println(data.substring(cnt));
      }//end else
    }//end for loop
  }//end display48
  //-----------------------------------------------------//
  
  //Utility method to display hex data 48 characters to
  // the line
  static void hexDisplay48(
                        ArrayList<Byte> binaryEncodedData){
    int charCnt = 0;
    for(Byte element : binaryEncodedData){
      System.out.print(
               Integer.toHexString((int)element & 0X00FF));
      charCnt++;
      if(charCnt%24 == 0){
        System.out.println();//new line
        charCnt = 0;
      }//end if
    }//end for-each
  }//end hexDisplay48
  //-----------------------------------------------------//
}//end class Huffman01
//=======================================================//

//An object of this class can be used to encode a raw text
// message using the Huffman encoding methodology.
class HuffmanEncoder{
  String rawData;  
  TreeSet <HuffTree>theTree = new TreeSet<HuffTree>();
  ArrayList <Byte>binaryEncodedData = 
                                     new ArrayList<Byte>();
  Hashtable <Character,Integer>frequencyData = 
                        new Hashtable<Character,Integer>();
  StringBuffer code = new StringBuffer();
  Hashtable <Character,String>huffEncodeTable;
  String stringEncodedData;
  Hashtable <String,Byte>encodingBitMap = 
                              new Hashtable<String,Byte>();
  //-----------------------------------------------------//
  
  //This method encodes an incoming String message using
  // the Huffman encoding methodology.  The method also
  // receives a reference to an empty data structure.
  // This data structures is populated with encoding 
  // particulars required later by the decode method 
  // to decode and transform the encoded message back 
  // into the original String message.  Note that in
  // order to keep this method simple, pad characters may
  // be appended onto the end of the original
  // message when it is encoded.  This is done to cause the
  // number of bits in the encoded message to be a multiple
  // of eight, thus causing the length of the encoded
  // message to be an integral number of bytes.  Additional
  // code would be required to avoid this at this point. 
  // However, it is easy to eliminate the extraneous
  // characters during decoding if the length of the
  // original message is known.
  ArrayList<Byte> encode(
              String rawData,
              Hashtable <Character,String>huffEncodeTable){
    //Save the incoming parameters.
    this.rawData = rawData;
    this.huffEncodeTable = huffEncodeTable;
    
    //For illustration purposes only, enable the following
    // two statements to display the original message as a 
    // stream of bits.  This can be visually compared with
    // a similar display for the encoded  message later to
    // illustrate the amount of compression provided by
    // the encoding process.
    /*
    System.out.println("Uncompressed Bit Data: ");
    displayRawDataAsBits();
    */
    //Create a frequency chart that identifies each of the
    // individual characters in the original message and
    // the number of times (frequency) that each character
    // appeared in the message.
    createFreqData();
    
    //For illustration purposes only, enable the following
    // statement to display the contents of the frequency
    // chart created above.

    //DISPLAY FREQUENCY
    displayFreqData();

    //Create a HuffLeaf object for each character
    // identified in the frequency chart.  Store the
    // HuffLeaf objects in a TreeSet object.  Each HuffLeaf
    // object encapsulates the character as well as the
    // number of times that the character appeared in the
    // original message (the frequency).
    createLeaves();
    
    //Assemble the HuffLeaf objects into  a Huffman tree
    // (a HuffTree object). A Huffman tree is a special
    // form of a binary tree  consisting of properly linked
    // HuffNode objects and HuffLeaf objects.
    //When the following method returns, the HuffTree
    // object remains as the only object stored in the
    // TreeSet object that previously contained all of the
    // HuffLeaf objects.  This is because all of the
    // HuffLeaf objects have been combined with HuffNode
    // objects to form the tree.
    createHuffTree();
    
    //Use the Huffman tree in a recursive manner to create
    // a bit code for each character in the message.  The
    // bit codes are different lengths with the shorter
    // codes corresponding to the characters with a high
    // frequency value and the longer codes corresponding
    // to the characters with the lower frequency values.
    //Note that the method call extracts the reference to
    // the Huffman tree from the TreeSet object and passes
    // that reference to the method.  This is necessary
    // because the method is recursive and cannot
    // conveniently work with the TreeSet object.
    //This method populates the data structure that is
    // required later to decode the encoded message.
    createBitCodes(theTree.first());

    //For purposes of illustration only, enable the 
    // following two statements to display a table showing
    // the relationship between the characters in the
    // original message and the bitcodes that will replace
    // those characters to produce the Huffman-encoded
    // message.

    System.out.println();
    displayBitCodes();

    //Encode the message into a String representation
    // of the bits that will make up the final encoded
    // message.  Also,the following method may optionally
    // display the String showing the bit values that will
    // appear in the final Huffman-encoded message.  This
    // is useful for comparing back against the bits in
    // the original message for purposes of evaluating the
    // amount of compression provided by encoding the
    // message.
    encodeToString();
    
    //Populate a lookup table that relates eight bits
    // represented as a String to every possible combinaion
    // of eight actual bits.
    buildEncodingBitMap();
    
    //Encode the String representation of the bits that
    // make up the encoded message to the actual bits
    // that make up the encoded message.
    //Note that this method doesn't handle the end of the
    // message very gracefully for those cases where the
    // number of required bits is not a multiple of 8.  It
    // simply adds enough "0" characters to the end to
    // cause the length to be a multiple of 8.  This may
    // result in extraneous characters at the end of the
    // decoded message later.
    //For illustration purposes only, this method may also
    // display the extended version of the String
    // representation of the bits for comparison with the
    // non-extended version.
    encodeStringToBits();
    
    //Return the encoded message.
    return binaryEncodedData;
  }//end encode method
  //-----------------------------------------------------//
  
  //This method displays a message string as a series of
  // characters each having a value of 1 or 0.
  void displayRawDataAsBits(){
    for(int cnt = 0,charCnt = 0;cnt < rawData.length();
                                          cnt++,charCnt++){
      char theCharacter = rawData.charAt(cnt);
      String binaryString = Integer.toBinaryString(theCharacter);
      //Append leading zeros as necessary to show eight
      // bits per character.
      while(binaryString.length() < 8){
        binaryString = "0" + binaryString;
      }//end while loop
      if(charCnt%6 == 0){
        //Display 48 bits per line.
        charCnt = 0;
      }//end if
      System.out.print(binaryString);
    }//end for loop
    System.out.println();
  }//end displayRawDataAsBits
  //-----------------------------------------------------//

  //This method creates a frequency chart that identifies
  // each of the individual characters in the original
  // message and the number of times that each character
  // appeared in the message.  The results are stored in
  // a Hashtable with the characters being the keys and the
  // usage frequency of each character being the
  // corresponding Hashtable value for that key.  
  void createFreqData(){
    for(int cnt = 0;cnt < rawData.length();cnt++){
      char key = rawData.charAt(cnt);
      if(frequencyData.containsKey(key)){
        int value = frequencyData.get(key);
        value += 1;
        frequencyData.put(key,value);
      }else{
        frequencyData.put(key,1);
      }//end else
    }//end for loop
  }//end createFreqData
  //-----------------------------------------------------//
  
  //This method displays the contents of the frequency
  // chart created by the method named createFreqData.
  void displayFreqData(){
    System.out.println("\nFrequency:");
    Enumeration <Character>enumerator = 
                                      frequencyData.keys();
    while(enumerator.hasMoreElements()){
      Character nextKey = enumerator.nextElement();
      System.out.println(
               nextKey + " " + frequencyData.get(nextKey));
    }//end while
  }//end displayFreqData
  //-----------------------------------------------------//
  
  //This method creates a HuffLeaf object for each char
  // identified in the frequency chart.  The HuffLeaf
  // objects are stored in a TreeSet object.  Each HuffLeaf
  // object encapsulates the character as well as the
  // number of times that the character appeared in the
  // original message.
  void createLeaves(){
    Enumeration <Character>enumerator = 
                                      frequencyData.keys();
    while(enumerator.hasMoreElements()){
      Character nextKey = enumerator.nextElement();
      theTree.add(new HuffLeaf(
                      nextKey,frequencyData.get(nextKey)));
    }//end while
  }//end createLeaves
  //-----------------------------------------------------//

  //This inner class is used to construct a leaf object in
  // the Huffman tree.
  class HuffLeaf extends HuffTree{
    
    private int value;
    
    //HuffLeaf constructor
    public HuffLeaf(int value, int frequency){
      this.value = value;
      //Note that frequency is inherited from HuffTree
      this.frequency = frequency;
    }//end HuffLeaf constructor
    
    public int getValue(){
      return value;
    }//end getValue
  
  }//End HuffLeaf class
  //=====================================================//
  
  //Assemble the HuffLeaf objects into a HuffTree object.
  // A HuffTree object is a special form of a binary tree
  // consisting of properly linked HuffNode objects and
  // HuffLeaf objects.
  //When the method terminates, the HuffTree object
  // remains as the only object stored in the TreeSet
  // object that previously contained all of the HuffLeaf
  // objects.  This is because all of the HuffLeaf
  // objects have been removed from the TreeSet object
  // and combined with HuffNode objects to form the
  // Huffman tree (as represented by the single HuffTree
  // object).
  //The method contains two sections of code that can be
  // enabled to display:
  // 1. The contents of the original TreeSet object.
  // 2. The contents of the TreeSet object for each
  //    iteration during which HuffLeaf objects are being
  //    combined with HuffNode objects to form the final
  //    HuffTree object.
  // This display is very useful for understanding how the
  // Huffman tree is constructed.  However, this code
  // should be enabled only for small trees because it
  // generates a very large amount of output.
  
  //The HuffTree object is constructed by:
  // 1. Extracting pairs of HuffLeaf or HuffNode objects
  //    from the TreeSet object in ascending order based
  //    on their frequency value. 
  // 2. Using the pair of extracted objects to construct
  //    a new HuffNode object where the two extracted
  //    objects become children of the new HuffNode
  //    object, and where the frequency value stored in
  //    the new HuffNode object is the sum of the
  //    frequency values in the two child objects.
  // 3. Removing the two original HuffLeaf or HuffNode
  //    objects from the TreeSet and adding the new
  //    HuffNode object to the TreeSet object.  The
  //    position of the new HuffNode object in the Treeset
  //    object is determined by its frequency value 
  //    relative to the other HuffNode or HuffLeaf objects
  //    in the collection. The new HuffNode object will 
  //    eventually become a child of another new HuffNode 
  //    object unless it ends up as the root of the 
  //    HuffTree object.
  // 4. Continuing this process until the TreeSet object
  //    contains a single object of type HuffTree.
  void createHuffTree() {
    //Enable the following statements to see the original
    // contents of the TreeSet object. Do this only for
    // small trees because it generates lots of output.
    
    System.out.println("\nDisplay Original TreeSet");
    Iterator <HuffTree> originalIter = theTree.iterator();
    while(originalIter.hasNext()){
      System.out.println("nHuffNode, HuffLeaf, or HuffTree");
      displayHuffTree(originalIter.next(),0);
    }//end while loop
    System.out.println();
    //End code to display the TreeSet
    
    //Iterate on the size of the TreeSet object until all
    // of the elements have been combined into a single
    // element of type HuffTree
    while(theTree.size() > 1){
      //Get, save, and remove the first two elements.
      HuffTree left = theTree.first();
      theTree.remove(left);
      HuffTree right = theTree.first();
      theTree.remove(right);
      
      //Combine the two saved elements into a new element
      // of type HuffNode and add it to the TreeSet
      // object.
      HuffNode tempNode = new HuffNode(left.getFrequency() 
                        + right.getFrequency(),left,right);
      theTree.add(tempNode);

      //Enable the following statements to see the HuffTree
      // being created from HuffNode and HuffLeaf objects.
      // Do this only for small trees because it will
      // generate a lot of output.
      
      System.out.println("\nDisplay Working TreeSet");
      Iterator <HuffTree> workingIter = theTree.iterator();
      while(workingIter.hasNext()){
        System.out.println("nHuffNode, HuffLeaf, or HuffTree");
        displayHuffTree(workingIter.next(),0);
      }//end while loop
      System.out.println();
      //End code to display the TreeSet
       
    }//end while
  }//end createHuffTree
  //-----------------------------------------------------//
  
  //Recursive method to display a HufTree object.  The
  // first call to this method should pass a value of 0
  // for recurLevel.
  void displayHuffTree(HuffTree tree,int recurLevel){
    recurLevel++;
    if(tree instanceof HuffNode){
      // This is a node, not a leaf.  Process it as a node.

      //Cast to type HuffNode.
      HuffNode node = (HuffNode)tree;
      // Get and save the left and right branches
      HuffTree left = node.getLeft();
      HuffTree right = node.getRight();
      
      //Display information that traces out the recursive
      // traversal of the tree in order to display the
      // contents of the leaves.
      System.out.print("  Left to " + recurLevel + " ");
      //Make a recursive call.
      displayHuffTree(left,recurLevel);
      
      System.out.print("  Right to " + recurLevel + " ");
      //Make a recursive call.
      displayHuffTree(right,recurLevel);
      
    }else{
      //This is a leaf.  Process it as such.
      //Cast the object to type HuffLeaf.
      HuffLeaf leaf = (HuffLeaf)tree;
      System.out.println(
                        "  Leaf:" + (char)leaf.getValue());
    }//end else
    
    System.out.print("  Back ");

  }//end displayHuffTree
  //-----------------------------------------------------//
  //This inner class is used to construct a node object in
  // the Huffman tree.
  class HuffNode extends HuffTree{
  
    private HuffTree left;
    private HuffTree right;
  
    //HuffNode constructor
    public HuffNode(
               int frequency,HuffTree left,HuffTree right){
      this.frequency = frequency;
      this.left = left;
      this.right = right;
    }//end HuffNode constructor
  
    public HuffTree getLeft(){
      return left;
    }//end getLeft
  
    public HuffTree getRight(){
      return right;
    }//end getRight
  
  }//end HuffNode class
  //=====================================================//

  //This method uses the Huffman tree in a recursive manner
  // to create a bitcode for each character in the message.
  // The bitcodes are different lengths with the shorter
  // bitcodes corresponding to the characters with a high
  // usage frequency value and the longer bitcodes
  // corresponding to the characters with the lower
  // frequency values.
  //Note that this method receives a reference to the
  // Huffman tree that was earlier contained as the only
  // object in the TreeSet object.  (Because this method is
  // recursive, it cannot conveniently work with the
  // TreeSet object.
  
  //This method creates a Huffman encoding table as a
  // Hashtable object that relates the variable length
  // bitcodes to the characters in the original message.
  // The bitcodes are constructed as objects of type
  // StringBuffer consisting of sequences of the characters
  // 1 and 0.
  //Each bitcode describes the traversal path from the root
  // of the Huffman tree to a leaf on that tree.  Each time
  // the path turns to the left, a 0 character is appended
  // onto the StringBuffer object and becomes part of the
  // resulting bitcode.  Each time the path turns to the
  // right, a 1 character is appended onto the
  // StringBuffer object.  When a leaf is reached, the
  // character stored in that leaf is retrieved and put
  // into the Hashtable object as a key.  A String
  // representation of the StringBuffer object is used as
  // the value for that key in the Hashtable.
  //At completion,the Hashtable object contains a series of
  // keys consisting of the original characters in the
  // message and a series of values as String objects
  // (consisting only of 1 and 0 characters) representing
  // the bitcodes that will eventually be used to encode
  // the original message.
  //Note that theHashtable object that is populated by this
  // method is the data structure that is required later
  // to decode the encoded message.
  void createBitCodes(HuffTree tree){
    if(tree instanceof HuffNode){
      // This is a node, not a leaf.  Process it as a node.

      //Cast to type HuffNode.
      HuffNode node = (HuffNode)tree;
      // Get and save the left and right branches
      HuffTree left = node.getLeft();
      HuffTree right = node.getRight();
      
      //Append a 0 onto the StringBuffer object.  Then make
      // a recursive call to this method passing a
      // reference to the left child as a parameter.  This
      // recursive call will work its way all the way down
      // to a leaf before returning.  Then it will be time
      // to process the right path.
      code.append("0");
      createBitCodes(left);
      
      //Return to here from recursive call on left child.

      //Delete the 0 from the end of the StringBuffer
      // object to restore the contents of that object to
      // the same state that it had before appending the 0
      // and making the recursive call on the left branch.
      //Now we will make a right turn.  Append a 1 to the
      // StringBuffer object and make a recursive call to
      // this method passing a reference to the right child
      // as a parameter.  Once again, this recursive call
      // will work its way all the  way down to a leaf
      // before returning.
      code.deleteCharAt(code.length() - 1);//Delete the 0.
      code.append("1");
      createBitCodes(right);
      
      //Return to here from recursive call on right child.

      //Delete the character most recently appended to the
      // StringBuffer object and return from this recursive
      // call to the method.  The character is deleted
      // because control is being transferred back one
      // level in the recursive process and the
      // StringBuffer object must be restored to the same
      // state that it had when this recursive call was
      // made.
      code.deleteCharAt(code.length() - 1);
    }else{
      //This is a leaf.  Process it as such.
      //Cast the object to type HuffLeaf.
      HuffLeaf leaf = (HuffLeaf)tree;
      
      //Put an entry into the Hashtable.  The Hashtable
      // key consists of the character value stored in the
      // leaf. The value in the Hashtable consists of the
      // contents of the StringBuffer object representing
      // the path from the root of the tree to the leaf.
      // This is the bitcode and is stored in the Hashtable
      // as a String consisting of only 1 and 0 characters.
      huffEncodeTable.put((char)(leaf.getValue()),code.toString());
    }//end else

  }//end createBitCodes
  //-----------------------------------------------------//
  
  //This method displays a table showing the relationship
  // between the characters in the original message and the
  // bitcodes that will ultimately replace each of those
  // characters to produce the Huffman-encoded message.
  void displayBitCodes(){
    System.out.println("Huffman BitCodes:");
    Enumeration <Character>enumerator = huffEncodeTable.keys();
    while(enumerator.hasMoreElements()){
      Character nextKey = enumerator.nextElement();
      System.out.println(nextKey + " " + huffEncodeTable.get(nextKey));
    }//end while
    /*System.out.println("\nUncompressed Bit Data: ");
    displayRawDataAsBits();
    System.out.println("Size Before Compression: " + rawData.length() * 8 + " bits");*/
  }//end displayBitCodes
  //-----------------------------------------------------//
  
  //This method encodes the message into a String
  // representation of the bits that will make up the final
  // encoded message.  The String consists of only 1 and 0
  // characters where each character represents the state
  // of one of the bits in the Huffman-encoded message.
  //Also for illustration purposes, this method optionally
  // displays the String showing the bit values that will
  // appear in the Huffman-encoded message.
  void encodeToString(){
    StringBuffer tempEncoding = new StringBuffer();
    for(int cnt = 0;cnt < rawData.length();cnt++){
      //Do a table lookup to get the substring that
      // represents the bitcode for each message character.
      // Append those substrings to the string that
      // represents the Huffman-encoded message.
      tempEncoding.append(huffEncodeTable.get(rawData.charAt(cnt)));
    }//end for loop
    
    //Convert the StringBuffer object to a String object.
    stringEncodedData = tempEncoding.toString();

    //For illustration purposes, enable the following two
    // statements to display the String showing the bit
    // values that will appear in the Huffman-encoded
    // message.  Display 48 bits to the line except for
    // the last line, which may be shorter, and which may
    // not be a multiple of 8 bits.
/*
    System.out.println("nString Encoded Data");
    display48(stringEncodedData);
*/
  }//end encodeToString
  //-----------------------------------------------------//

  //This method populates a lookup table that relates eight
  // bits represented as a String to eight actual bits for
  // all possible combinations of eight bits.
  void buildEncodingBitMap(){

    for(int cnt = 0; cnt <= 255;cnt++){
      StringBuffer workingBuf = new StringBuffer();
      if((cnt & 128) > 0){workingBuf.append("1");
        }else{workingBuf.append("0");};
      if((cnt & 64) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      if((cnt & 32) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      if((cnt & 16) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      if((cnt & 8) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      if((cnt & 4) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      if((cnt & 2) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      if((cnt & 1) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      encodingBitMap.put(workingBuf.toString(),
                                              (byte)(cnt));
    }//end for loop
  }//end buildEncodingBitMap
  //-----------------------------------------------------//
  
  //The purpose of this method is to create actual bit data
  // that matches the 1 and 0 characters in the 
  // stringEncodedData that represents bits with the 1 and
  // 0 characters.
  //Note that this method doesn't handle the end of the
  // data very gracefully for those cases where the number
  // of required bits is not a multiple of 8.  It simply
  // adds enough "0" characters to the end to cause the
  // length to be a multiple of 8.  This may result in
  // extraneous characters at the end of the decoded
  // message later. However, it isn't difficult to remove
  // the extraneous characters at decode time as long as
  // the length of the original message is known.
  //For illustration purposes, this method may optionally
  // display the extended version of the stringEncodedData
  // for comparison with the non-extended version.
  //Note that the binary Huffman-encoded data produced by
  // this method is stored in a data structure of type
  // ArrayList <Byte>.
  void encodeStringToBits(){
    //Extend the length of the stringEncodedData to cause
    // it to be a multiple of 8.
    int remainder = stringEncodedData.length()%8;
    for(int cnt = 0;cnt < (8 - remainder);cnt++){
      stringEncodedData += "0";
    }//end for loop
    
    //For illustration purposes only, enable the following
    // two statements to display the extended 
    // stringEncodedData in the same format as the 
    // original stringEncodedData.
/*
    System.out.println("nExtended String Encoded Data");
    display48(stringEncodedData);
*/
    //Extract the String representations of the required
    // eight bits.  Generate eight actual matching bits by
    // looking the bit combination up in a table.
    for(int cnt = 0;cnt < stringEncodedData.length();
                                                 cnt += 8){
      String strBits  = stringEncodedData.substring(
                                                cnt,cnt+8);
      byte realBits = encodingBitMap.get(strBits);
      binaryEncodedData.add(realBits);
    }//end for loop
  }//end encodeStringToBits
  //-----------------------------------------------------//
  
  //Method to display a String 48 characters to the line.
  void display48(String data){
    for(int cnt = 0;cnt < data.length();cnt += 48){
      if((cnt + 48) < data.length()){
        //Display 48 characters.
        System.out.println(data.substring(cnt,cnt+48));
      }else{
        //Display the final line, which may be short.
        System.out.println(data.substring(cnt));
      }//end else
    }//end for loop
  }//end display48
  //-----------------------------------------------------//
  
}//end HuffmanEncoder class
//=======================================================//


//An object of this class can be used to decode a
// Huffman-encoded message given the encoded message, 
// a data structure containing particulars as to how the
// original message was encoded, and the length of the
// original message..
class HuffmanDecoder{
  Hashtable <String,Character>huffDecodeTable = 
                         new Hashtable<String,Character>();
  String stringDecodedData;
  String decodedData = "";
  Hashtable <Byte,String>decodingBitMap = 
                              new Hashtable<Byte,String>();
  ArrayList <Byte>binaryEncodedData;
  
  //The following structure contains particulars as to how
  // the original message was encoded, and must be received
  // as an incoming parameter to the decode method along
  // with the encoded message and the length of the
  // original message.
  Hashtable <Character,String>huffEncodeTable;
  //Used to eliminate the extraneous characters on the end.
  int rawDataLen;
  //-----------------------------------------------------//
  
  //This method receives a Huffman-encoded message along
  // with a data structure containing particulars as to how
  // the original message was encoded and the length of the
  // original message.  It decodes the original message and
  // returns the decoded version as a String object.
  String decode(ArrayList <Byte>binaryEncodedData,
               Hashtable <Character,String>huffEncodeTable,
               int rawDataLen){
    //Save the incoming parameters.
    this.binaryEncodedData = binaryEncodedData;
    this.huffEncodeTable = huffEncodeTable;
    this.rawDataLen = rawDataLen;
    
    //Create a decoding bit map, which is essentially the
    // reverse of the encoding bit map that was used to
    // encode the original message.
    buildDecodingBitMap();
    
    //Decode the encoded message from a binary
    // representation to a String of 1 and 0 characters
    // that represent the actual bits in the encoded
    // message.  Also, for illustration purposes only,
    // this method may optionally display the String.
    decodeToBitsAsString();
    
    //Create a Huffman decoding table by swapping the keys
    // and values from the Huffman encoding table received
    // as an incoming parameter by the decode method.
    buildHuffDecodingTable();
    
    //Decode the String containing only 1 and 0 characters
    // that represent the bits in the encoded message. This
    // produces a replica of the original message that was
    // subjected to Huffman encoding.  Write the resulting
    // decoded message into a String object referred to by
    // decoded data.
    decodeStringBitsToCharacters();
    
    //Return the decoded message.  Eliminate the extraneous
    // characters from the end of the message on the basis
    // of the known length of the original message.
    return decodedData.substring(0,rawDataLen);    
  }//end decode method
  //-----------------------------------------------------//

  //This method populates a lookup table that relates eight
  // bits represented as a String to eight actual bits for
  // all possible combinations of eight bits.  This is
  // essentially a reverse lookup table relative to the
  // encodingBitMap table that is used to encode the
  // message.  The only difference between the two is a
  // reversal of the key and the value in the Hashtable
  // that contains the table.
  
  void buildDecodingBitMap(){
    for(int cnt = 0; cnt <= 255;cnt++){
      StringBuffer workingBuf = new StringBuffer();
      if((cnt & 128) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      if((cnt & 64) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      if((cnt & 32) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      if((cnt & 16) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      if((cnt & 8) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      if((cnt & 4) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      if((cnt & 2) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      if((cnt & 1) > 0){workingBuf.append("1");
        }else {workingBuf.append("0");};
      decodingBitMap.put((byte)(cnt),workingBuf.
                                               toString());
    }//end for loop
  }//end buildDecodingBitMap()
  //-----------------------------------------------------//
  
  //This method decodes the encoded message from a binary
  // representation to a String of 1 and 0 characters that
  // represent the actual bits in the encoded message.
  // Also, for illustration purposes only, this method
  // may optionally display that String.
  void decodeToBitsAsString(){
    StringBuffer workingBuf = new StringBuffer();

    for(Byte element : binaryEncodedData){
      byte wholeByte = element;
      workingBuf.append(decodingBitMap.get(wholeByte));
    }//end for-each
    
    //Convert from StringBuffer to String
    stringDecodedData = workingBuf.toString();
    
    //For illustration purposes only, enable the following
    // two statements to display the String containing 1
    // and 0 characters that represent the bits in the
    // encoded message.

    System.out.println("\nCompressed Bit Data:");
    display48(stringDecodedData);
    System.out.println("Size After Compression: " + binaryEncodedData.size() * 8 + " bits");
    //System.out.println("\nCompression factor: " + compressionFactor);
  }//end decodeToBitsAsString
  //-----------------------------------------------------//
  
  //This method creates a Huffman decoding table by
  // swapping the keys and the values from the Huffman
  // encoding table received as an incoming parameter by
  // the decode method.
  void buildHuffDecodingTable(){
    Enumeration <Character>enumerator = 
                                    huffEncodeTable.keys();
    while(enumerator.hasMoreElements()){
      Character nextKey = enumerator.nextElement();
      String nextString = huffEncodeTable.get(nextKey);
      huffDecodeTable.put(nextString,nextKey);
    }//end while
  }//end buildHuffDecodingTable()
  //-----------------------------------------------------//

  //The method begins with an empty StringBuffer object
  // referred to by the variable named workBuf and another
  // empty StringBuffer object referred to by the variable
  // named output.  The StringBuffer object referred to by
  // output is used to construct the decoded message.  The
  // StringBuffer object referred to by workBuf is used as
  // a temporary holding area for substring data.
  //The method reads the String containing only 1 and 0
  // characters that represent the bits in the encoded
  // message (stringDecodedData).  The characters are read
  // from this string one character at a time.  As each new
  // character is read, it is appended to the StringBuffer
  // object referred to by workBuf.
  //As each new character is appended to the StringBuffer
  // object, a test is performed to determine if the
  // current contents of the StringBuffer object match one
  // of the keys in a lookup table that relates strings
  // representing Huffman bitcodes to characters in the
  // original message.
  //When a match is found, the value  associated with that
  // key is extracted and appended to the StringBuffer
  // object referred to by output.  Thus, the output text
  // is built up one character at a time.
  //Having processed the matching key, A new empty
  // StringBuffer object is instantiated, referred to by
  // workBuf, and the process of reading, appending, and
  // testing for a match is repeated until all of the
  // characters in the string that represents the bits in
  // the encoded message have been processed.  At that
  // point, the StringBuffer object referred to by output
  // contains the entire decoded message.  It is converted
  // to type String and written into the object referred to
  // by decodedData.  Then the method returns with the task
  // of decoding the encoded message having been completed.
  void decodeStringBitsToCharacters(){
    StringBuffer output = new StringBuffer();
    StringBuffer workBuf = new StringBuffer();

    for(int cnt = 0;cnt < stringDecodedData.length();
                                                    cnt++){
      workBuf.append(stringDecodedData.charAt(cnt));
      if(huffDecodeTable.containsKey(workBuf.toString())){
        output.append(
                  huffDecodeTable.get(workBuf.toString()));
        workBuf = new StringBuffer();
        //System.out.println(output);
      }//end if
    }//end for loop
    
    decodedData = output.toString();
  }//End decodeStringBitsToCharacters();
  //-----------------------------------------------------//
  
  //Method to display a String 48 characters to the line.
  void display48(String data){
    for(int cnt = 0;cnt < data.length();cnt += 48){
      if((cnt + 48) < data.length()){
        //Display 48 characters.
        System.out.println(data.substring(cnt,cnt+48));
      }else{
        //Display the final line, which may be short.
        System.out.println(data.substring(cnt));
      }//end else
    }//end for loop
  }//end display48
  //-----------------------------------------------------//

}//end HuffmanDecoder class
//=======================================================//

//This class is the abstract superclass of the
// HuffNode and HuffLeaf classes.  Objects instantiated
// from HuffNode and HuffLeaf are populated and used to
// create a Huffman tree.
abstract class HuffTree implements Comparable{

  int frequency;
  
  public int getFrequency(){
    return frequency;
  }//end getFrequency

  //This method compares this object to an object whose
  // reference is received as an incoming parameter.
  // The method guarantees that sorting processes that
  // depend on this method, such as TreeSet objects, will
  // sort the objects into a definitive order.
  
  // If the frequency values of the two objects are
  // different, the sort is based on the frequency values.
  // If the frequency values are equal, the objects are
  // sorted based on their relative hashCode values. 
  // Thus, if the same two objects with the same frequency
  // value are compared two or more times during the
  // execution of the program, those two objects will
  // always be sorted into the same order.  There is no
  // chance of an ambiguous tie as to which object
  // should be first except for the case where an object
  // is compared to itself using two references to the
  // same object.
  public int compareTo(Object obj){
    HuffTree theTree = (HuffTree)obj;
    if (frequency == theTree.frequency){
      //The objects are in a tie based on the frequency
      // value.  Return a tiebreaker value based on the
      // relative hashCode values of the two objects.
      return (hashCode() - theTree.hashCode());
    }else{
      //Return negative or positive as this frequency is
      // less than or greater than the frequency value of
      // the object referred to by the parameter.
      return frequency - theTree.frequency;
    }//end else
  }//end compareTo

}//end HuffTree class
//=======================================================//