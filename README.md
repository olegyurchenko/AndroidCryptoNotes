# AndroidCryptoNotes
Some data (such as credit card numbers or full passport data) is dangerous to store in clear.

It is doubly dangerous to store such data on a mobile gadget (phone), which can be lost or easily fall into the wrong hands.

The *CryptoNotes* application allows you to encrypt / decrypt any text with AES (Advanced Encryption Standard) algorithm.

The resulting encrypted test can already be stored, sent by mail or stored in the cloud.

Encryption / decryption is compatible with the OpenSSL utility. 

The encryption result will be similar to running openssl utilities:
```shell
openssl enc-aes-256-cbc -a
```

Decryption will be similar to:
```shell
ppenssl enc -aes-256-cbc -a -d
```
