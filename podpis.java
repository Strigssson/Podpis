import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class podpis {
    public static void main(String[] args) throws IOException {
            if (args.length != 0) {
                switch (args[0]) {
                    case "-s": {
                        if (args.length < 2) {
                            System.out.println("You did not provide file to sign.");
                        } else if (args.length == 2) {
                            Path pathToFile = Paths.get(args[1]);
                            File existsTest = pathToFile.toFile();
                            File fileToSign = pathToFile.getFileName().toFile();
                            if (existsTest.exists()) {
                                System.out.println("You are performing signature of the file: " + fileToSign);
                                byte[] fileContent = Files.readAllBytes(pathToFile);
                                System.out.println("Performing hashing...");
                                String hashedFile = hashFunction(fileContent).toString();
                                //System.out.println("Generating new keys for encryption...");
                                //int[] keys = keyGen();
                                //System.out.println("Performing encrypting of hash...");
                                //String encryptedHash = encrypt(hashedFile, keys[1], keys[0], keys [2]).toString();
                                System.out.println("Performing signature creation...");
                                Path pathToSignatureFile = createSignature(hashedFile);//(encryptedHash);
                                System.out.println("Zipping files...");
                                zipFiles(pathToSignatureFile, pathToFile);
                                System.out.println("Job done.");
                            } else {
                                System.out.println("File doesnt exist.");
                            }
                        } else {
                            System.out.println("Too many parameters." + args.length);
                        }

                        break;
                    }
                    case "-v": {

                        Path unzipDir = Path.of("C:/Users/Acer/Desktop/unzipDir");

                        if (args.length < 2) {
                            System.out.println("You did not provide files to verify");
                        } else if (args.length == 2) {
                            Path pathToZip = Paths.get(args[1]);
                            File existsTest = pathToZip.toFile();
                            if (existsTest.exists()) {
                                RandomAccessFile raf = new RandomAccessFile(args[1], "r");
                                long n = raf.readInt();
                                raf.close();
                                if (n != 0x504B0304) {
                                    System.out.println("Please provide a .zip file!");
                                    break;
                                }
                            } else {
                                System.out.println("Provided file doesnt exist.");
                                break;
                            }
                            System.out.println("You are performing verification of file's signature.");
                            System.out.println("Performing unzipping...");
                            Path[] helpPaths = unZipFiles(pathToZip, unzipDir);

                            StringBuffer bufferForSuffix = new StringBuffer();
                            String unzipedFileToSign = "filetosign";

                            bufferForSuffix.append(unzipedFileToSign);
                            bufferForSuffix.append(helpPaths[1]);


                            Path unzipDirFile = Path.of("C:/Users/Acer/Desktop/unzipDir/" + bufferForSuffix);
                            Path unzipDirSignature = Path.of("C:/Users/Acer/Desktop/unzipDir/signature.txt");
                           // System.out.println("Performing decrypting of hashed signature...");
                            //decrypt(unzipDirSignature);
                            hashMatch(unzipDirFile, unzipDirSignature);

                        } else {
                            System.out.println("Too many parameters.");
                        }

                        break;
                    }

                    default: {
                        System.out.println("You have to choose switch '-s' for signature, or '-v' for verification of signature.");
                    }
                }
            }
        }

public static StringBuffer hashFunction(byte[] fileContent){
        StringBuffer result = new StringBuffer();
        byte[] hashed = new byte [128];
        int lengthOfFileContent = fileContent.length;
        int i = 0;
        int j = 0;
        int hash = 7;
        int primeNumber = 43;
        char nch;

        //naplnenie nulami
        for (int k = 0; k < 128; k++){
            hashed[i] = 0;
        }

        //naplnenie hodnotami bytov zo suboru
        while (lengthOfFileContent != 0){
            lengthOfFileContent = lengthOfFileContent - 1;
            if (i <= 127){
                hashed[i] = (byte) (hashed[i] + fileContent[j]);
            } else {
                i = 0;
                hashed[i] = (byte) (hashed[i] + fileContent[j]);
            }
            i++;
            j++;
        }

        for ( i = 0; i < 128; i++){

            hashed[i] = (byte) (hashed[i]+((i*primeNumber))+hash*primeNumber*(fileContent.length % primeNumber));

           if (hashed[i] < 33){
                while (hashed[i] < 33){
                 hashed[i] += 93;
                }
                nch = (char) hashed[i];
                result.append(nch);
            }else if (hashed[i] > 126){
                while (hashed[i] > 126){
                    hashed[i] -= 93;
                }
               nch = (char) hashed[i];
               result.append(nch);
            }else {
               nch = (char) hashed[i];
               result.append(nch);
            }
        }
        return result;
}

public static Path createSignature(String signature) throws IOException {
    //vytvorenie file
    Path signatureFilePath = Path.of("C:/Users/Acer/Desktop/podpis.txt");

    File signatureFile = new File("C:/Users/Acer/Desktop/podpis.txt");
    signatureFile.createNewFile();
    System.out.println("File podpis.txt was successfully created in path:" + signatureFile.getAbsolutePath());

    //zapisanie podpisu do file
    FileWriter writer = new FileWriter("C:/Users/Acer/Desktop/podpis.txt");
    writer.write(signature);
    writer.close();
    System.out.println("File podpis.txt was successfully signed.");

    return signatureFilePath;
}

public static Path zipFiles(Path signatureFilePath, Path fileToSignPath) throws IOException {
    Path zipPath = Path.of("C:/Users/Acer/Desktop/zipedFiles.zip");

    FileOutputStream fos = new FileOutputStream("C:/Users/Acer/Desktop/zipedFiles.zip");
    ZipOutputStream zos = new ZipOutputStream(fos);

    String fileName = String.valueOf(fileToSignPath.getFileName());
    String signatureFileName = String.valueOf(signatureFilePath.getFileName());

    zos.putNextEntry(new ZipEntry(String.valueOf(new File("signature.txt"))));
    byte[] bytesSign = Files.readAllBytes(signatureFilePath);
    zos.write(bytesSign, 0, bytesSign.length);
    zos.closeEntry();

    StringBuffer buffer = new StringBuffer();
    String fileToSign = "filetosign";

    String name = String.valueOf(fileToSignPath.getFileName());
    int lastIndexOf = name.lastIndexOf(".");


    buffer.append(fileToSign);
    buffer.append(name.substring(lastIndexOf));

    zos.putNextEntry(new ZipEntry(String.valueOf(new File(buffer.toString()))));
    byte[] bytesFile = Files.readAllBytes(fileToSignPath);
    zos.write(bytesFile, 0, bytesFile.length);
    zos.closeEntry();

    zos.close();


    System.out.println("File " + fileName + " was added into zipedFiles.zip archive with its signature " + signatureFileName + " in path: " + zipPath);

    return zipPath;
}

public static Path[] unZipFiles(Path zipPath, Path unzipDir) throws IOException {

    File dir = unzipDir.toFile();

    if (!dir.exists()) {
        dir.mkdirs();
    }
    FileInputStream fis = new FileInputStream(String.valueOf(zipPath));
    byte[] buffer = new byte[1024];

    ZipInputStream zis = new ZipInputStream(fis);
    ZipEntry ze = zis.getNextEntry();


    int i = 0;
    Path[] paths = new Path[2];

    paths[0] = dir.toPath();

    while( ze != null ){

        String fileName = ze.getName();

        if( i != 0 ){
            int lastIndexOf = fileName.lastIndexOf(".");
            paths[1] = Path.of(fileName.substring(lastIndexOf));

        }

        File newFile = new File(unzipDir + File.separator + fileName);

        System.out.println("Unzipping to " + dir);
        new File(newFile.getParent()).mkdirs();
        FileOutputStream fos = new FileOutputStream(newFile);
        int len;
        while ((len = zis.read(buffer)) > 0){
            fos.write(buffer, 0, len);
        }
        fos.close();
        zis.closeEntry();
        ze = zis.getNextEntry();
        i++;
    }
    zis.closeEntry();
    zis.close();
    fis.close();

    return paths;

}

public static void hashMatch(Path fileToSign, Path signedFile) throws IOException {

    // file a jeho hash

    System.out.println("Computing hash of the file...");
    byte[] fileContent = Files.readAllBytes(fileToSign);
    String hashedFile = String.valueOf(hashFunction(fileContent));

    //signature read hashu zo suboru
    System.out.println("Getting hash from signature file...");
    File signature = new File(String.valueOf(signedFile));
    Scanner reader = new Scanner(signature);
    String data = null;
    while (reader.hasNextLine()){
        data = reader.nextLine();
    }
    reader.close();

    System.out.println("Performing hash comparision...");

    if (hashedFile.equals(data)){
        System.out.println("Verification of signature was successfull.");
    }else{
        System.out.println("Verification of signature was unsuccessfull.");
    }
}

/*
public static int[] keyGen(){

    int[] primeNumbers = new int[] {2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,
            73,79,83,89,97,101,103,107,109,113,127,131,137,139,149,151,157,163,167,173,
            179,181,191,193,197,199,211,223,227,229,233,239,241,251,257,263,269,271,277,281,
            283,293,307,311,313,317,331,337,347,349,353,359,367,373,379,383,389,397,401,409,
            419,421,431,433,439,443,449,457,461,463,467,479,487,491,499,503,509,521,523,541};

    Random random = new Random();
    int pi = random.nextInt(99 - 0) + 0;
    int qi = random.nextInt( 99 - 0) + 0;
    if ( pi == qi){
        while ( pi == qi ){
            qi = random.nextInt(99 -0) +0;
        }
    }
    int p = primeNumbers[pi];
    int q = primeNumbers[qi];

    int n = p*q;
    double ptmp = p-1;
    double qtmp = q-1;

    while (qtmp % ptmp != 0.0){
        qtmp += (q-1);
    }
    int arraySize = (int) qtmp;
    int[] coprimeNumbers = new int[arraySize];
    int j = 0;

    for (int i = 1; i < qtmp; i++){
        if ( (qtmp % i) != 0){
            coprimeNumbers[j] = i;
            j++;
        }
    }

    int[] helparray = new int[coprimeNumbers.length];
    int k = 0;
    for (int i = 0; i< coprimeNumbers.length; i++){
        for(j = 0; j < primeNumbers.length; j++){
            if (coprimeNumbers[i] == primeNumbers[j]){
                helparray[k] = coprimeNumbers[i];
                k++;
            }
        }
    }
    int ei = random.nextInt( k);
    int e = helparray[ei];

    int temp = (int) (1 + qtmp);

    while ((temp%e) != 0){
        temp += qtmp;

    }
    int d = temp / e;


    int[] keys = new int[3];

    keys[0] = n;
    keys[1] = e;
    keys[2] = d;

 return keys;

}

public static StringBuffer encrypt(String hashedtext, int e, int n, int d){

    byte[] hashedTextByte = hashedtext.getBytes(StandardCharsets.UTF_8);
    BigInteger helpSum;
    BigInteger tempVal;
    BigInteger eKey = BigInteger.valueOf(e);
    BigInteger nKey = BigInteger.valueOf(n);



    StringBuffer encryptedHash = new StringBuffer();
    System.out.println(e + " e is");
    System.out.println(n + " n is");
    System.out.println(d + " d is");
    for(int i = 0; i < hashedtext.length(); i++){
        tempVal = new BigInteger(String.valueOf(hashedTextByte[i]));
        helpSum = (tempVal.pow(e)).mod(nKey);
        int helpnch = helpSum.intValue();
        char nch = (char) helpnch;

        encryptedHash.append(nch);
        System.out.println(tempVal + " encrypted to " + nch + " nch" );
    }
    encryptedHash.append(".");
    encryptedHash.append(d);
    encryptedHash.append(",");
    encryptedHash.append(n);
    return encryptedHash;
}

public static void decrypt(Path signedFile) throws FileNotFoundException {
    File signature = new File(String.valueOf(signedFile));
    Scanner reader = new Scanner(signature);
    String data = null;
    while (reader.hasNextLine()){
        data = reader.nextLine();
    }
    reader.close();

    int lastIndexOfComma = data.lastIndexOf(",");

    StringBuffer keyN = new StringBuffer();
    for (int i = lastIndexOfComma+1; i < data.length(); i++){
        char nch = data.charAt(i);
        keyN.append(nch);
    }

    int lastIndexOfDot = data.lastIndexOf(".");

    StringBuffer keyD = new StringBuffer();
    for (int i = lastIndexOfDot+1; i < lastIndexOfComma; i++){
        char nch = data.charAt(i);
        keyD.append(nch);
    }
    String stringD = keyD.toString();
    int d = Integer.parseInt(stringD);



    String stringN = keyN.toString();
    int intN = Integer.parseInt(stringN);
    BigInteger n = BigInteger.valueOf(intN);


    byte[] hashedEncrypted = data.getBytes(StandardCharsets.UTF_8);
    BigInteger helpSum;
    BigInteger tempVal;
    BigInteger nch;

    StringBuffer decryptedHash = new StringBuffer();

    for(int i = 0; i < lastIndexOfDot; i++){
        tempVal = new BigInteger(String.valueOf(hashedEncrypted[i]));
        helpSum = (tempVal.pow(d)).mod(n);

        nch = helpSum;
        decryptedHash.append(nch);
    }
    System.out.println(decryptedHash);
    }

*/
}
