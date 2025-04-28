package dev.cnpe.ventescaposbe.security.exception

class IdpUserNotFoundException(
    val identifier: String
) : IdpAccessException("User not found in IdP using identifier: $identifier")