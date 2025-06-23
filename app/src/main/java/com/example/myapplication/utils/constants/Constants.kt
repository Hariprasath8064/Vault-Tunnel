package com.example.myapplication.utils.constants

object Constants {
    // App-wide constants
    const val REQUEST_VERSION = "1"

    // Database related constants
    object Database {
        // Table names
        const val TABLE_CERTIFICATES = "Certificates"
        const val TABLE_COMMUNICATION_PROPERTIES = "CommunicationProperties"
        const val TABLE_SENSITIVE_DATA = "SensitiveData"

        // Column names
        const val COLUMN_ID = "id"
        const val COLUMN_TYPE = "type"
        const val COLUMN_VALUE = "value"
        const val COLUMN_CIPHER_TEXT = "cipherText"
        const val COLUMN_IV = "iv"

        // Database properties
        const val DATABASE_NAME = "AppData.db"
        const val DATABASE_VERSION = 1

        // Logging tag
        const val TAG = "DatabaseInitializer"
        const val TAG_SECURE_DATA_STORE = "SecureDataStore"
    }

    //HttpUtil related constants


    //AppInitialization related constants
    object Appinit {
        const val PREF_NAME = "app_init_prefs"
        const val INIT_KEY = "isAppInitialized"

    }

    // KeyStore related constants

    object KeyOps {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG = "StrongBoxCryptoManager"
        const val GCM_TAG_LENGTH = 128
        const val ERROR_KEY_RETRIEVE = "Error retrieving secret key"
        const val ERROR_ENCRYPTION = "Encryption failed"
        const val ERROR_DECRYPTION = "Decryption failed"
        const val ALIAS = "my_app_secret_key"
        const val KM_TAG = "KeyManager"
    }

    //KeyGeneration related constants

    object KeyGen {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TAG = "StrongBoxKeyGenerator"
        const val KEY_SIZE_AES = 256
        const val KEY_SIZE_RSA = 2048
        const val ERROR_KEY_EXISTS = "Key with alias \"%s\" already exists."
        const val ERROR_KEY_PAIR_EXISTS = "Key pair with alias \"%s\" already exists."
        const val ERROR_KEY_GEN_FAILED = "Key generation failed"
        const val ERROR_KEY_PAIR_GEN_FAILED = "Key pair generation failed"
        const val STRONGBOX_NOT_AVAILABLE = "StrongBox not available, falling back to TEE"
        const val STRONGBOX_KEY_GEN = "Attempting to generate StrongBox-backed key."
        const val STRONGBOX_KEY_PAIR_GEN = "Attempting to generate StrongBox-backed key pair."
        const val STRONGBOX_KEY_SUCCESS = "StrongBox-backed key with alias \"%s\" generated."
        const val STRONGBOX_KEY_PAIR_SUCCESS = "StrongBox-backed key pair with alias \"%s\" generated."
        const val TEE_KEY_SUCCESS = "TEE-backed key with alias \"%s\" generated."
        const val TEE_KEY_PAIR_SUCCESS = "TEE-backed key pair with alias \"%s\" generated."
    }


    //csr generation related constants

    object Csr {
        const val TAG = "CsrGenerator"
        const val AES_WRAPPER_ALIAS = "csr_key_wrapper"
        const val TEMP_KEY_ID = "temp_private_key"
        const val TEMP_PUBLIC_KEY_ID = "temp_public_key"
        const val TEMP_KEYS_TABLE = "temp_keys"
        const val PUBLIC_KEY_TYPE = "PUBLIC_KEY"
        const val ERROR_KEY_GEN_STORE = "Key generation or storage failed"
        const val ERROR_KEYS_NOT_FOUND = "Stored keys not found, generating new keys"
        const val ERROR_CSR_GEN = "CSR generation failed"
        const val CSR_SUBJECT = "CN=com.example.basehttpapp"
        const val CSR_ALGORITHM = "SHA256withRSA"
        const val CSR_KEY_ALGORITHM = "RSA"
        const val CSR_KEY_SIZE = 2048
        const val CSR_SUCCESS = "CSR generated successfully, length: %d"
        const val TEMP_KEY_PAIR_LOG = "Creating temporary key pair for CSR generation"
        const val TEMP_KEY_PAIR_STORED = "Temporary key pair stored securely"
        const val AES_KEY_EXISTS = "AES wrapper key already exists, using existing key"
    }
}