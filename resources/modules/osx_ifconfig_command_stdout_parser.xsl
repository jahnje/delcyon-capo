<?xml version="1.0" encoding="UTF-8"?>
<!--  
    This will parse osx ifconfig output into somewhat decent xml.
    improvements welcome    
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:server="http://www.delcyon.com/capo-server"
    xmlns:client="http://www.delcyon.com/capo-client"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:output method="xml" indent="yes" />
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>
    
    <!-- This holds a copy of the output tokenized into lines -->
    <xsl:variable name="line_tree">
        <xsl:apply-templates select="//stdout" mode="phase1"/>
    </xsl:variable>
    
    <!-- This parses the output and tokenizes it into an element per line -->
    <xsl:template name="line_template" match="//stdout" mode="phase1">
        <xsl:variable name="line" select="tokenize(.,'\n')"/>
        <xsl:element name="lines">
            <xsl:for-each select="$line">
                <xsl:element name="line">
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>
    
    <!-- This is the main entry point -->
    <xsl:template match="client:command">        
        <xsl:apply-templates select="$line_tree" mode="phase2"/>
    </xsl:template>
    
    <!-- this template parses out the lines that are dev/link entries -->
    <xsl:template match="/" mode="phase2">
        <!-- we run through all of the lines first so we can have a searchable tree with indexs for each of the dev/link entries -->
        <xsl:variable name="devindex">
            <devindex>
                <xsl:for-each select="lines/line">
                    <xsl:if test="matches(.,'^[a-z]+\d+:.*')">
                        <dev>
                            <xsl:attribute name="index" select="position()"/>
                        </dev>
                    </xsl:if>
                </xsl:for-each>
                <!-- add one extra entry so we don't get a index out of range when we process the last link -->
                <dev index="{count(lines/line)+1}"/>
            </devindex>
        </xsl:variable>
        
        <!-- go over all of the lines and create a link element for each line that matches ^[a-z]+\d+:.* example: en0: -->
        <xsl:for-each select="lines/line">
            <xsl:variable name="line_index" select="position()"/>
            <xsl:if test="matches(.,'^[a-z]+\d+:.*')">              
                    <link>
                        <!-- break the link line up -->
                        <xsl:variable name="line" select="tokenize(.,'[\s+|=]')"/>
                        <!-- itterare over the pieces of the line -->
                        <xsl:for-each select="$line">
                            <!-- save a copy of the current position, so we can use it below -->
                            <xsl:variable name="index" select="position()"/>
                            <xsl:choose>
                                <!-- dev name -->
                                <xsl:when test="position() = 1">
                                    <xsl:attribute name="dev" select="replace(.,':','')"/>
                                </xsl:when>
                                <!-- every thing else is a name value pair basically, clean up/replace any weird chars while were there as well -->
                                <xsl:when test="position() mod 2 = 0">
                                    <xsl:attribute name="{.}" select="replace(replace($line[$index + 1],'&lt;',','),'&gt;','')"/>
                                </xsl:when>
                            </xsl:choose>
                        </xsl:for-each>
                        <!-- once we have the atributes set on our link element process all of the lines again, with an index bracket so we can treat them as children -->
                        <xsl:call-template name="link_attribute_template">
                            <xsl:with-param name="link_position" select="position()"/>
                            <xsl:with-param name="next_link_position">
                                <xsl:value-of select="$devindex/devindex/dev[@index = $line_index]/following-sibling::dev[1]/@index"/>
                            </xsl:with-param>                           
                        </xsl:call-template>                        
                    </link>    
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    
    <!-- 
        run through all of the lines between $link_position and $next_link_position an create attributes for each entry
        This is called from the link template
    -->
    <xsl:template name="link_attribute_template">      
        <xsl:param name="link_position"/>
        <xsl:param name="next_link_position"/>
        <xsl:for-each select="$line_tree/lines/line">
            <xsl:variable name="line" select="normalize-space(.)"/>
            <!-- skip all of the inet lines since we want to make them child elements, attributes must always be processed first -->
            <xsl:if test="matches($line,'inet.*') = false()">
                <xsl:choose>
                    <!-- make sure we are in our bracket, could probably move this into the for-each -->
                    <xsl:when test="position() > $link_position and position() &lt; $next_link_position and string-length($line) > 0 ">
                        <!-- start all of the line specific matches -->
                        <xsl:choose>
                            <!-- handle any line that startes with a name: as a single attribute for the whole line -->
                            <xsl:when test="matches($line,'[a-z]:\s.*')">
                                <xsl:variable name="tokenized_line" select="tokenize($line,':')"/>
                                <!-- Reaplce any spaces that are left in attribute names with a dot just to be safe, exmple: 'supported media' -->
                                <xsl:attribute name="{replace($tokenized_line[1],'\s+','.')}" select="replace(normalize-space($tokenized_line[2]),'[&gt;|&lt;]','')"/>
                            </xsl:when>
                            <!-- tun devices seem to be associated with a PID, so treat it as a special entry -->
                            <xsl:when test="matches($line,'open.*')">                           
                                <xsl:attribute name="open" select="replace(replace($line,'open\s+',''),'[\(|\)]','')"/>
                            </xsl:when>
                            <!-- every thing else is a name value pair -->
                            <xsl:otherwise>
                                <xsl:variable name="tokenized_line" select="tokenize($line,'\s+')"/>
                                <xsl:for-each select="$tokenized_line">
                                    <xsl:variable name="index" select="position()"/>
                                    <xsl:if test="position() mod 2 = 0">
                                        <!-- point ot point devices use an arrow symbol in place of text to describe the local end, seriously WTF, so we replace it with 'via' on general principles -->                                        
                                        <xsl:attribute name="{replace($tokenized_line[$index - 1],'--&gt;','via')}" select="normalize-space(.)"/>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>                    
                    </xsl:when>
                </xsl:choose>
            </xsl:if>
        </xsl:for-each>
        <!-- process the lines again, this time looking for inet* lines. each of these will become child elements, since a link can have more than one address -->
        <xsl:for-each select="$line_tree/lines/line">
            <xsl:variable name="line" select="normalize-space(.)"/>
            <xsl:if test="matches($line,'inet.*') = true()">
                <xsl:if test="position() > $link_position and position() &lt; $next_link_position and string-length($line) > 0 ">
                    <xsl:element name="addr">
                        <xsl:variable name="tokenized_line" select="tokenize($line,'\s+')"/>
                        <xsl:for-each select="$tokenized_line">
                            <xsl:variable name="index" select="position()"/>
                            <!-- all addr lines can be processed as name/value pairs -->
                            <xsl:if test="position() mod 2 = 0">
                                <!-- point ot point devices use an arrow symbol in place of text to describe the local end, seriously WTF, so we replace it with 'via' on general principles -->
                                <xsl:attribute name="{replace($tokenized_line[$index - 1],'--&gt;','via')}" select="normalize-space(.)"/>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:element>
                </xsl:if>
            </xsl:if>
        </xsl:for-each>
        
    </xsl:template>
    
</xsl:stylesheet>
