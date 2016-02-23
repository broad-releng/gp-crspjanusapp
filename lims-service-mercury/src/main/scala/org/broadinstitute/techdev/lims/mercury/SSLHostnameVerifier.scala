package org.broadinstitute.techdev.lims.mercury

import sun.security.util.HostnameChecker
import java.security.Principal
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSession
import javax.security.auth.kerberos.KerberosPrincipal
import grizzled.slf4j.Logging

/**
 * Verify hostname of SSL Certificates
 */
class SSLHostnameVerifier extends HostnameVerifier with Logging {

  def verify(hostname: String, sslSession: SSLSession): Boolean = {
    val checker: HostnameChecker = HostnameChecker.getInstance(HostnameChecker.TYPE_TLS)
    var validCertificate = false
    var validPrincipal = false
    try {
      val peerCertificates: Array[Certificate] = sslSession.getPeerCertificates
      if (peerCertificates.length > 0 && peerCertificates(0).isInstanceOf[X509Certificate]) {
        val peerCertificate: X509Certificate = peerCertificates(0).asInstanceOf[X509Certificate]
        try {
          checker.`match`(hostname, peerCertificate)
          validCertificate = true
        }
        catch {
          case ex: CertificateException =>
            error("Certificate does not match hostname!", ex)
        }
      }
      else
        warn("Peer doesn't have any certs or they aren't X.509")
    }
    catch {
      case ex: SSLPeerUnverifiedException => {
        try {
          val peerPrincipal: Principal = sslSession.getPeerPrincipal
          if (peerPrincipal.isInstanceOf[KerberosPrincipal])
            validPrincipal = HostnameChecker.`match`(hostname, peerPrincipal.asInstanceOf[KerberosPrincipal])
          else
            warn("Cannot verify principal, not Kerberos")
        }
        catch {
          case sslex: SSLPeerUnverifiedException =>
            error("Can't verify principal since there isn't one", sslex)
        }
      }
    }
    validCertificate || validPrincipal
  }
}
