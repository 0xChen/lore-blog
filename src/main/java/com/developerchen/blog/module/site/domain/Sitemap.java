package com.developerchen.blog.module.site.domain;

import org.unbescape.xml.XmlEscape;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sitemap
 *
 * @author syc
 */
public class Sitemap implements Serializable {

    /**
     * The document statement.
     */
    private static final String DOCUMENT_STATEMENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";


    /**
     * Start url set tag.
     */
    private static final String START_URL_SET_TAG = "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">";

    /**
     * End url set tag.
     */
    private static final String END_URL_SET_TAG = "</urlset>";

    /**
     * Url set.
     */
    private List<Url> urlSetList;


    public Sitemap() {
        urlSetList = Collections.emptyList();
    }

    /**
     * @param size url size.
     */
    public Sitemap(int size) {
        urlSetList = new ArrayList<>(size);
    }

    public void addUrl(String loc, String lastMod) {
        urlSetList.add(new Url(loc, lastMod));
    }


    @Override

    public String toString() {
        return "Sitemap{" +
                "urlSet=" + urlSetList +
                '}';
    }

    public String toXmlString() {
        StringBuilder builder = new StringBuilder(DOCUMENT_STATEMENT);
        builder.append(START_URL_SET_TAG);
        for (Url url : urlSetList) {
            builder.append(url.toXmlString());
        }
        builder.append(END_URL_SET_TAG);
        return builder.toString();
    }

    private static class Url {

        /**
         * Start URL tag.
         */
        private static final String START_URL_TAG = "<url>";

        /**
         * End URL tag.
         */
        private static final String END_URL_TAG = "</url>";

        /**
         * Start loc tag.
         */
        private static final String START_LOC_TAG = "<loc>";

        /**
         * End loc tag.
         */
        private static final String END_LOC_TAG = "</loc>";

        /**
         * Start last mod tag.
         */
        private static final String START_LAST_MOD_TAG = "<lastmod>";

        /**
         * End last mod tag.
         */
        private static final String END_LAST_MOD_TAG = "</lastmod>";

        String loc;
        String lastMod;

        Url(String loc, String lastMod) {
            this.loc = loc;
            this.lastMod = lastMod;
        }

        @Override
        public String toString() {
            return "Url{" +
                    "loc='" + loc + '\'' +
                    ", lastMod='" + lastMod + '\'' +
                    '}';
        }

        String toXmlString() {
            StringBuilder builder = new StringBuilder();
            builder.append(START_URL_TAG);
            builder.append(START_LOC_TAG);
            builder.append(XmlEscape.escapeXml10(loc));
            builder.append(END_LOC_TAG);
            builder.append(START_LAST_MOD_TAG);
            builder.append(lastMod);
            builder.append(END_LAST_MOD_TAG);
            builder.append(END_URL_TAG);
            return builder.toString();
        }
    }

}
