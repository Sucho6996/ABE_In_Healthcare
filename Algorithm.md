Algorithm

1. Setup Algorithm:
- **Input:** Implicit Security Parameter
- **Define universal Attribute Set C = {A1, A2, …, An}**
- Generate random number a<sub>i</sub></b> from <b>Z<sub>q</sub>* for each i</b> <b>in</b> u<b>.</b>
- Accordingly, AA computes public key for each attribute i as PKi = <b>a<sub>i</sub> .B</b>
- <b>Next, AA chooses</b> one master secret key a <b>from Z<sub>q</sub>*.</b>
- The master public key computed PK = a.B
- The public parameters (C, PKi, PK) are distributed.
- The system master keys are kept hidden.
- **Output:** (PARAMS, SMK)
1. **Encryption Algorithm:**
- **Input:** The message M, public parameters; access structure.
- Choose one random <b>k</b> from <b>Z<sub>q</sub>*.</b>
- Define polynomial q<sub>x</sub> with degree k<sub>x</sub>-1 corresponding to each node x of T.
- For the root node set q<sub>r</sub>(0) = k.
- Define q<sub>r</sub> uniquely by choosing other k<sub>r</sub>-1 random points from <b>Z<sub>q</sub>*.</b>
- for any arbitrary node x in T do 
  - Set q<sub>x</sub>(0) = q <sub>parent (x)</sub> (index (x))
  - To define q<sub>x</sub> uniquely, k<sub>x</sub> – 1 other random points are chosen from <b>Z<sub>q</sub>*.</b>
- **end for**
- Session keys are computed by using ECC based Scaler point Multiplication as SK=k.PK= (k<sub>x</sub>, k<sub>y</sub> ) where k<sub>x</sub> is the encryption key.
- AES\_KEY = SHA256 (K<sub>x</sub>)
- Encrypt Message (Cm) = AES <sub>ENC</sub> (M, AES\_KEY)
- Let the set of leaf nodes of the tree T be u
- for each i = attr(x) in u **do**
  - C<sub>i</sub> = q<sub>x</sub>(0)<b><sup>.</sup> PK</b><sub>i</sub>
- **End for**
- The ciphertext CT = (T, Cm, Ci) [Cm is the Encrypted file, Ci is the Encrypted Key]
- **Output** CT

1. **Key Generation Algorithm:**
- **Input:** The recipient’s Json Web Token (JWT);
- At First the staff service authenticates the JWT and extract unique identity (U<sub>id</sub>) as well as retrieve **the set of attributes (A)** possessed by the user.
- The staff service transmits the U<sub>id</sub> and the attribute set (A) to the **Attribute Authority**.
- If (A! = Null)
  - D<sub>i</sub> = Hash (U<sub>id</sub>) \* a \* a<sub>i</sub><sup>-1</sup> for all i in A [a = Master Secret Key, a<sub>i</sub><sup>-1</sup> = **modular multiplicative inverse** of attribute secret key]
  - D.put (i, D<sub>i</sub>)
- Else 
  - Return
- End if
- **Output** D
1. **Decryption Algorithm:**
- **Input:** The ciphertext bundle CT = (T, Cm, Ci) from patient’s database, the user’s secret key map **D** from **Key Gen Algo** and system’s public parameters PARAMS.
- **Function** DECRYPT\_KEY (CT, D, x) {
  - If **node** x is a leaf node associated with attribute i = attr (x) then
    - **If** D.get(i)! = NULL **then**
      - Compute and Return: ECC based scaler point multiplication of the Di with Ci
    - **Else return NULL**
    - **End If**
  - **Else**
    - **For** each child y of x 
      - DECRYPT\_KEY (CT, D, y)
    - End for
    - Suppose Cx is the set of decrypted child node of x.
    - **For** each y ∈ Cx 
      - **Σ** y ∈ Cx Δ <sub>i, j</sub> (0) \* q<sub>y</sub> (0) \* a \* B
    - End For
  - End If
- End Function}
- A = DECRYPT\_KEY (CT, D, R) // R is the root node of access tree T
- If (A! = NULL) 
- Discard K’<sub>y</sub> and extract K’<sub>x</sub> for decrypting the image.
- AES\_KEY = SHA256 (K’<sub>x</sub>)
- M’ = AES <sub>DEC</sub> (Cm, AES\_KEY). // y has 2 different values but x is unique.
  - **Output** M’

End If 
