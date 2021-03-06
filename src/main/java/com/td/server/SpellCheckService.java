package com.td.server;

import com.td.bktree.BKTree;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpellCheckService extends HttpServlet {
    private ConcurrentHashMap<Long, BKTree> spellCheckMap = new ConcurrentHashMap<Long, BKTree>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        Map<String, String[]> params = request.getParameterMap();
        if (!params.containsKey("id")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("param id not found");
            return;
        }
        if (!params.containsKey("word")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("param word not found");
            return;
        }


        long id = 0;
        try {
            id = Long.parseLong(params.get("id")[0]);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("id not type long");
        }
        int editDistance = 1;
        if (params.containsKey("edit_distance")) {
            try {
                int value = Integer.parseInt(params.get("edit_distance")[0]);
                editDistance = value > 0 ? value : 1;
            } catch (Exception e) {

            }
        }
        if (spellCheckMap.containsKey(id)) {
            List<String> results = spellCheckMap.get(id).search(params.get("word")[0], editDistance);
            try {
                response.getWriter().println(new Response(results).toString());
            } catch (JSONException e) {
                e.printStackTrace();
                response.getWriter().println("Something is wrong with the service right now...");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("id not found");
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        Map<String, String[]> params = request.getParameterMap();
        if (!params.containsKey("words")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("param words not found");
            return;
        }

        if (params.containsKey("id")) {
            long id = 0;
            try {
                id = Long.parseLong(params.get("id")[0]);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("id not type long");
            }
            if (spellCheckMap.containsKey(id)) {
                BKTree bkTree = spellCheckMap.get(id);
                for (String s : params.get("words")[0].split(",")) {
                    bkTree.add(s.trim());
                }
                response.getWriter().println("new words added");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("id not found");
            }
        } else {
            long id = new Date().getTime();
            BKTree bkTree = new BKTree();
            for (String s : params.get("words")[0].split(",")) {
                bkTree.add(s.trim());
            }
            JSONObject object = new JSONObject();
            try {
                object.put("id", id);
                response.getWriter().println(object.toString());
                spellCheckMap.put(id, bkTree);
            } catch (JSONException e) {
                e.printStackTrace();
                response.getWriter().println("Something is wrong with the service right now...");
            }
        }
    }
}
