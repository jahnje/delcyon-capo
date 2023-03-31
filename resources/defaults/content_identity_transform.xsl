<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="xml" indent="yes" />
    <xsl:strip-space elements="*"/>
    <xsl:template match="@*|node()" mode="children">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"  mode="children"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="node()">                
        <xsl:apply-templates select="child::node()" mode="children"/>        
    </xsl:template>
</xsl:stylesheet>
