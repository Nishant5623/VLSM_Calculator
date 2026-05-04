import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class VLSMCalculator extends JFrame {

    // ── Palette ──────────────────────────────────────────────────
    static final Color BG       = new Color(0x07, 0x0a, 0x0e);
    static final Color BG1      = new Color(0x0d, 0x11, 0x17);
    static final Color BG2      = new Color(0x13, 0x19, 0x20);
    static final Color BG3      = new Color(0x1a, 0x23, 0x32);
    static final Color BORDER   = new Color(255,255,255, 18);
    static final Color BORDER2  = new Color(255,255,255, 30);
    static final Color ACCENT   = new Color(0x00, 0xc8, 0xff);
    static final Color ACCENT2  = new Color(0x00, 0x87, 0xb8);
    static final Color TEXT     = new Color(0xe8, 0xed, 0xf3);
    static final Color TEXT2    = new Color(0x7a, 0x8f, 0xa6);
    static final Color TEXT3    = new Color(0x4a, 0x5f, 0x74);
    static final Color GREEN    = new Color(0x00, 0xe5, 0xa0);
    static final Color AMBER    = new Color(0xff, 0xb5, 0x47);
    static final Color RED      = new Color(0xff, 0x5f, 0x6d);

    static final Color[] SUBNET_COLORS = {
        new Color(0x00,0xc8,0xff), new Color(0x00,0xe5,0xa0),
        new Color(0xff,0xb5,0x47), new Color(0xff,0x7e,0xb3),
        new Color(0xa7,0x8b,0xfa), new Color(0xfb,0x92,0x3c),
        new Color(0x34,0xd3,0x99), new Color(0x60,0xa5,0xfa),
        new Color(0xf4,0x72,0xb6), new Color(0xfb,0xbf,0x24)
    };

    static Font MONO, MONO_SM, MONO_LG, SANS_TITLE, SANS_HEAD, SANS_SM;

    static {
        try {
            Font base = Font.createFont(Font.TRUETYPE_FONT,
                VLSMCalculator.class.getResourceAsStream("/fonts/JetBrainsMono.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base);
            MONO    = base.deriveFont(13f);
            MONO_SM = base.deriveFont(11f);
            MONO_LG = base.deriveFont(22f);
        } catch (Exception e) {
            MONO    = new Font("Consolas", Font.PLAIN, 13);
            MONO_SM = new Font("Consolas", Font.PLAIN, 11);
            MONO_LG = new Font("Consolas", Font.PLAIN, 22);
        }
        SANS_TITLE = new Font("Segoe UI", Font.BOLD,   32);
        SANS_HEAD  = new Font("Segoe UI", Font.BOLD,   15);
        SANS_SM    = new Font("Segoe UI", Font.PLAIN,  12);
    }

    // ── State ─────────────────────────────────────────────────────
    private final List<SubnetReq> requirements = new ArrayList<>();
    private final List<SubnetResult> results   = new ArrayList<>();

    // ── UI refs ───────────────────────────────────────────────────
    private JTextField   tfNetwork, tfCidr;
    private JPanel       reqPanel, resultsArea;
    private JLabel       lblCount, lblError;
    private AddressMapPanel mapPanel;
    private JPanel       summaryPanel;
    private JScrollPane  mainScroll;

    // ─────────────────────────────────────────────────────────────
    public VLSMCalculator() {
        super("VLSM Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 640));
        setPreferredSize(new Dimension(1100, 760));
        setBackground(BG);
        buildUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Build ─────────────────────────────────────────────────────
    private void buildUI() {
        JPanel root = new DarkPanel(BG);
        root.setLayout(new BorderLayout(0,0));

        root.add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(), buildRightPanel());
        split.setDividerLocation(340);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setBackground(BG);
        split.setContinuousLayout(true);
        root.add(split, BorderLayout.CENTER);

        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);

        // default data
        addRow("Engineering", "60");
        addRow("Sales",       "30");
        addRow("Servers",     "14");
        addRow("Management",  "10");
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new DarkPanel(BG1);
        p.setLayout(new BorderLayout());
        p.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0, BORDER),
            new EmptyBorder(24, 32, 24, 32)
        ));

        JPanel left = new DarkPanel(BG1);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        // badge
        RoundLabel badge = new RoundLabel("● NETWORKING TOOL", MONO_SM, ACCENT,
                new Color(0,200,255,20), new Color(0,200,255,50));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(badge);
        left.add(vStrut(10));

        // title
        JPanel titleRow = new DarkPanel(BG1);
        titleRow.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel t1 = new JLabel("VLSM ");
        t1.setFont(SANS_TITLE); t1.setForeground(TEXT);
        JLabel t2 = new JLabel("Subnet");
        t2.setFont(SANS_TITLE); t2.setForeground(ACCENT);
        JLabel t3 = new JLabel(" Calculator");
        t3.setFont(SANS_TITLE); t3.setForeground(TEXT);
        titleRow.add(t1); titleRow.add(t2); titleRow.add(t3);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(titleRow);
        left.add(vStrut(6));

        JLabel sub = new JLabel("Variable Length Subnet Masking — allocate IP blocks by exact host requirements.");
        sub.setFont(MONO); sub.setForeground(TEXT2);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(sub);
        left.add(vStrut(10));

        JPanel tags = new DarkPanel(BG1);
        tags.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
        for (String t : new String[]{"IPv4","CIDR","RFC 950","Network Design"})
            tags.add(new RoundLabel(t, MONO_SM, TEXT3, BG2, BORDER));
        tags.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(tags);

        p.add(left, BorderLayout.WEST);

        // right side count
        JPanel right = new DarkPanel(BG1);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setAlignmentX(Component.RIGHT_ALIGNMENT);
        lblCount = new JLabel("—");
        lblCount.setFont(MONO_LG.deriveFont(Font.BOLD, 36f));
        lblCount.setForeground(ACCENT);
        lblCount.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel lbl2 = new JLabel("subnets allocated");
        lbl2.setFont(MONO_SM); lbl2.setForeground(TEXT3);
        lbl2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(lblCount); right.add(Box.createVerticalStrut(4)); right.add(lbl2);
        p.add(right, BorderLayout.EAST);

        return p;
    }

    // ── Left panel ────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel p = new DarkPanel(BG);
        p.setLayout(new BorderLayout());
        p.setBorder(new MatteBorder(0,0,0,1, BORDER));

        JPanel inner = new DarkPanel(BG) {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                if (getParent() != null) d.width = Math.max(d.width, getParent().getWidth());
                return d;
            }
        };
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Network config card
        inner.add(buildCard("Network Configuration", buildNetworkConfig()));
        inner.add(vStrut(14));

        // Requirements card
        inner.add(buildCard("Subnet Requirements", buildRequirementsPanel()));
        inner.add(vStrut(14));

        // Algorithm card
        inner.add(buildCard("Algorithm", buildAlgorithmPanel()));
        inner.add(vStrut(14));

        // Action buttons
        inner.add(buildActionRow());

        // Error
        lblError = new JLabel(" ");
        lblError.setFont(MONO_SM);
        lblError.setForeground(RED);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(vStrut(8));
        inner.add(lblError);
        inner.add(vGlue());

        JScrollPane sp = new JScrollPane(inner);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollbar(sp);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildNetworkConfig() {
        JPanel p = new DarkPanel(BG1);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(label10("PARENT NETWORK"));
        p.add(vStrut(6));

        JPanel row = new DarkPanel(BG1);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        tfNetwork = darkField("192.168.10.0");
        tfCidr    = darkField("/24");
        tfCidr.setMaximumSize(new Dimension(72, 36));
        tfCidr.setPreferredSize(new Dimension(72, 36));
        row.add(tfNetwork);
        row.add(Box.createHorizontalStrut(8));
        row.add(tfCidr);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(row);
        return p;
    }

    private JPanel buildRequirementsPanel() {
        JPanel wrapper = new DarkPanel(BG1);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JPanel topRow = new DarkPanel(BG1);
        topRow.setLayout(new BorderLayout());
        JLabel lbl = label10("SUBNETS");
        topRow.add(lbl, BorderLayout.WEST);
        JButton btnAdd = ghostButton("+ Add");
        btnAdd.addActionListener(e -> addRow("", ""));
        topRow.add(btnAdd, BorderLayout.EAST);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(topRow);
        wrapper.add(vStrut(8));

        reqPanel = new DarkPanel(BG1);
        reqPanel.setLayout(new BoxLayout(reqPanel, BoxLayout.Y_AXIS));
        reqPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(reqPanel);
        return wrapper;
    }

    private JPanel buildAlgorithmPanel() {
        JPanel p = new DarkPanel(BG1);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        String[] steps = {
            "Sort subnets by host count, largest first",
            "Find minimum power-of-2 block per requirement",
            "Align each block to its natural boundary",
            "Pack blocks sequentially into parent network"
        };
        for (int i = 0; i < steps.length; i++) {
            JPanel row = new DarkPanel(BG1);
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            JLabel num = new JLabel(String.format("%02d", i+1));
            num.setFont(MONO_SM); num.setForeground(ACCENT);
            num.setPreferredSize(new Dimension(22, 16));
            JLabel txt = new JLabel(steps[i]);
            txt.setFont(MONO_SM); txt.setForeground(TEXT2);
            row.add(num); row.add(Box.createHorizontalStrut(8)); row.add(txt);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(row);
            if (i < steps.length-1) p.add(vStrut(8));
        }
        return p;
    }

    private JPanel buildActionRow() {
        JPanel p = new DarkPanel(BG);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton clear = ghostButton("Clear");
        clear.addActionListener(e -> clearAll());
        clear.setPreferredSize(new Dimension(80, 36));
        clear.setMaximumSize(new Dimension(80, 36));

        JButton calc = accentButton("Calculate →");
        calc.addActionListener(e -> calculate());
        calc.setPreferredSize(new Dimension(200, 36));
        calc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        p.add(clear); p.add(Box.createHorizontalStrut(8)); p.add(calc);
        return p;
    }

    // ── Right panel ───────────────────────────────────────────────
    private JScrollPane buildRightPanel() {
        resultsArea = new DarkPanel(BG) {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                if (getParent() != null) d.width = Math.max(d.width, getParent().getWidth());
                return d;
            }
        };
        resultsArea.setLayout(new BoxLayout(resultsArea, BoxLayout.Y_AXIS));
        resultsArea.setBorder(new EmptyBorder(20,20,20,20));
        showEmptyState();

        JScrollPane sp = new JScrollPane(resultsArea);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollbar(sp);
        mainScroll = sp;
        return sp;
    }

    private void showEmptyState() {
        resultsArea.removeAll();
        JPanel emp = new DarkPanel(BG);
        emp.setLayout(new BoxLayout(emp, BoxLayout.Y_AXIS));
        emp.add(Box.createVerticalGlue());
        JLabel icon = new JLabel("⬡");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 48));
        icon.setForeground(BG3);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel txt = new JLabel("<html><center>Configure your network and<br>hit Calculate to see subnet allocations.</center></html>");
        txt.setFont(MONO_SM); txt.setForeground(TEXT3);
        txt.setAlignmentX(Component.CENTER_ALIGNMENT);
        emp.add(icon);
        emp.add(Box.createVerticalStrut(12));
        emp.add(txt);
        emp.add(Box.createVerticalGlue());
        emp.setPreferredSize(new Dimension(500, 300));
        emp.setMaximumSize(new Dimension(500, 300));
        emp.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsArea.add(emp);
        resultsArea.revalidate(); resultsArea.repaint();
    }

    // ── Footer ────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new DarkPanel(BG1);
        p.setLayout(new BorderLayout());
        p.setBorder(new CompoundBorder(
            new MatteBorder(1,0,0,0, BORDER),
            new EmptyBorder(12,32,12,32)
        ));
        JLabel left = new JLabel("Built in Java Swing  ·  Implements RFC 950 VLSM allocation");
        left.setFont(MONO_SM); left.setForeground(TEXT3);
        p.add(left, BorderLayout.WEST);

        JPanel btns = new DarkPanel(BG1);
        btns.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        JButton exp = linkButton("Export CSV");
        exp.addActionListener(e -> exportCSV());
        btns.add(exp);
        p.add(btns, BorderLayout.EAST);
        return p;
    }

    // ── Row management ────────────────────────────────────────────
    private void addRow(String name, String hosts) {
        SubnetReq req = new SubnetReq(name, hosts);
        requirements.add(req);

        JPanel row = new DarkPanel(BG1);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        req.nameField = darkField(name.isEmpty() ? "" : name);
        req.nameField.setFont(MONO.deriveFont(12f));
        req.nameField.setMinimumSize(new Dimension(0, 30));

        req.hostField = darkField(hosts.isEmpty() ? "" : hosts);
        req.hostField.setFont(MONO.deriveFont(12f));
        req.hostField.setMaximumSize(new Dimension(90, 30));
        req.hostField.setPreferredSize(new Dimension(90, 30));

        JButton del = new JButton("×");
        del.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        del.setForeground(TEXT3);
        del.setBackground(BG2);
        del.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        del.setPreferredSize(new Dimension(28,28));
        del.setMaximumSize(new Dimension(28,28));
        del.setFocusPainted(false);
        del.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        del.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { del.setForeground(RED); del.setBackground(new Color(255,95,109,20)); }
            public void mouseExited(MouseEvent e)  { del.setForeground(TEXT3); del.setBackground(BG2); }
        });
        del.addActionListener(e -> {
            requirements.remove(req);
            reqPanel.remove(row);
            reqPanel.revalidate(); reqPanel.repaint();
        });

        row.add(req.nameField);
        row.add(Box.createHorizontalStrut(6));
        row.add(req.hostField);
        row.add(Box.createHorizontalStrut(6));
        row.add(del);

        reqPanel.add(row);
        reqPanel.add(vStrut(6));
        reqPanel.revalidate(); reqPanel.repaint();
    }

    private void clearAll() {
        requirements.clear();
        reqPanel.removeAll(); reqPanel.revalidate(); reqPanel.repaint();
        results.clear();
        lblCount.setText("—");
        showEmptyState();
    }

    // ── VLSM logic ────────────────────────────────────────────────
    private void calculate() {
        lblError.setText(" ");

        String addrStr = tfNetwork.getText().trim();
        String cidrStr = tfCidr.getText().replace("/","").trim();

        int prefix;
        try { prefix = Integer.parseInt(cidrStr); }
        catch (NumberFormatException e) { showError("Prefix must be a number (1–30)."); return; }
        if (prefix < 1 || prefix > 30) { showError("Prefix must be 1–30."); return; }

        long baseInt = ipToLong(addrStr);
        if (baseInt < 0) { showError("Invalid network address."); return; }

        List<SubnetReq> reqs = new ArrayList<>();
        for (SubnetReq r : requirements) {
            String nm = r.nameField.getText().trim();
            String hs = r.hostField.getText().trim();
            int h;
            try { h = Integer.parseInt(hs); } catch (NumberFormatException e) { showError("Host counts must be valid numbers."); return; }
            if (h < 1) { showError("Host count must be ≥ 1."); return; }
            reqs.add(new SubnetReq(nm.isEmpty() ? "Subnet" : nm, hs));
            reqs.get(reqs.size()-1).hosts = h;
        }
        if (reqs.isEmpty()) { showError("Add at least one subnet requirement."); return; }

        reqs.sort((a,b) -> b.hosts - a.hosts);

        long netMask    = cidrToMask(prefix);
        long networkAddr = (baseInt & netMask) & 0xFFFFFFFFL;
        long totalAddrs  = (1L << (32 - prefix));
        long current     = networkAddr;

        results.clear();
        for (SubnetReq req : reqs) {
            int bits = bitsNeeded(req.hosts);
            int subPrefix = 32 - bits;
            if (subPrefix < prefix) { showError('"' + req.name + "\" needs /" + subPrefix + " but parent is /" + prefix + "."); return; }
            long size   = (1L << bits);
            long subMask = cidrToMask(subPrefix);
            long aligned = ((current + size - 1) / size) * size;
            if (aligned + size - 1 > networkAddr + totalAddrs - 1) { showError("Address space exhausted. Use a larger parent network."); return; }
            long net   = aligned;
            long bcast = net + size - 1;
            SubnetResult sr = new SubnetResult();
            sr.name      = req.name;
            sr.requested = req.hosts;
            sr.usable    = (int)(size - 2);
            sr.prefix    = subPrefix;
            sr.mask      = longToIp(subMask);
            sr.network   = longToIp(net);
            sr.first     = longToIp(net + 1);
            sr.last      = longToIp(bcast - 1);
            sr.broadcast = longToIp(bcast);
            sr.size      = size;
            sr.netLong   = net;
            results.add(sr);
            current = bcast + 1;
        }

        renderResults(networkAddr, totalAddrs, prefix, addrStr);
    }

    // ── Render ────────────────────────────────────────────────────
    private void renderResults(long networkAddr, long totalAddrs, int prefix, String addrStr) {
        lblCount.setText(String.valueOf(results.size()));
        resultsArea.removeAll();
        resultsArea.setBorder(new EmptyBorder(20,20,20,20));

        // Address map
        JPanel mapCard = buildCard("Address Space Map", null);
        mapPanel = new AddressMapPanel(results, networkAddr, totalAddrs);
        mapPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Legend
        JPanel legend = new DarkPanel(BG1);
        legend.setLayout(new WrapLayout(FlowLayout.LEFT, 12, 6));
        legend.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < results.size(); i++) {
            Color c = SUBNET_COLORS[i % SUBNET_COLORS.length];
            JPanel item = new DarkPanel(BG1);
            item.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
            JLabel dot = new JLabel("■");
            dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            dot.setForeground(c);
            JLabel nm = new JLabel(results.get(i).name);
            nm.setFont(MONO_SM); nm.setForeground(TEXT2);
            item.add(dot); item.add(nm);
            legend.add(item);
        }
        long used = results.stream().mapToLong(r -> r.size).sum();
        long free = totalAddrs - used;
        if (free > 0) {
            JPanel item = new DarkPanel(BG1);
            item.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
            JLabel dot = new JLabel("■");
            dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            dot.setForeground(BG3);
            JLabel nm = new JLabel("Free (" + free + " addr)");
            nm.setFont(MONO_SM); nm.setForeground(TEXT3);
            item.add(dot); item.add(nm);
            legend.add(item);
        }

        JPanel mapInner = new DarkPanel(BG1);
        mapInner.setLayout(new BoxLayout(mapInner, BoxLayout.Y_AXIS));
        mapInner.add(mapPanel);
        mapInner.add(vStrut(10));
        mapInner.add(legend);

        // rebuild map card with content
        mapCard = buildCard("Address Space Map — " + longToIp(networkAddr) + "/" + prefix, mapInner);
        mapCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsArea.add(mapCard);
        resultsArea.add(vStrut(14));

        // Subnet cards
        for (int i = 0; i < results.size(); i++) {
            resultsArea.add(buildSubnetCard(results.get(i), i));
            resultsArea.add(vStrut(10));
        }

        // Summary
        resultsArea.add(buildSummaryPanel(networkAddr, totalAddrs, prefix, addrStr));
        resultsArea.add(vStrut(20));

        resultsArea.revalidate();
        resultsArea.repaint();
        SwingUtilities.invokeLater(() -> mainScroll.getVerticalScrollBar().setValue(0));
    }

    private JPanel buildSubnetCard(SubnetResult s, int idx) {
        Color color = SUBNET_COLORS[idx % SUBNET_COLORS.length];

        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG1);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                // left accent
                g2.setColor(color);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(0,0,0,0));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // top row
        JPanel top = new DarkPanel(new Color(0,0,0,0));
        top.setOpaque(false);
        top.setLayout(new BorderLayout());
        top.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0, BORDER),
            new EmptyBorder(12, 18, 12, 18)
        ));

        JPanel nameRow = new DarkPanel(new Color(0,0,0,0));
        nameRow.setOpaque(false);
        nameRow.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JLabel dot = new JLabel("■");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dot.setForeground(color);
        JLabel nm = new JLabel(s.name);
        nm.setFont(SANS_HEAD); nm.setForeground(TEXT);
        JLabel cidrLbl = new JLabel(s.network + "/" + s.prefix);
        cidrLbl.setFont(MONO); cidrLbl.setForeground(ACCENT);
        nameRow.add(dot); nameRow.add(nm); nameRow.add(cidrLbl);

        JPanel rightRow = new DarkPanel(new Color(0,0,0,0));
        rightRow.setOpaque(false);
        rightRow.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JLabel hostsLbl = new JLabel(s.requested + " req · " + s.usable + " usable");
        hostsLbl.setFont(MONO_SM); hostsLbl.setForeground(TEXT2);
        JButton copy = smallButton("copy");
        copy.addActionListener(e -> {
            String txt = String.join("\n",
                "Network: "   + s.network + "/" + s.prefix,
                "Mask: "      + s.mask,
                "First IP: "  + s.first,
                "Last IP: "   + s.last,
                "Broadcast: " + s.broadcast
            );
            Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(txt), null);
            copy.setText("copied!");
            copy.setForeground(GREEN);
            javax.swing.Timer t = new javax.swing.Timer(1800, ev -> { copy.setText("copy"); copy.setForeground(TEXT3); });
            t.setRepeats(false); t.start();
        });
        rightRow.add(hostsLbl); rightRow.add(copy);

        top.add(nameRow, BorderLayout.WEST);
        top.add(rightRow, BorderLayout.EAST);

        // data grid
        String[][] cells = {
            {"NETWORK ADDRESS", s.network,   "CIDR",          "/" + s.prefix,  "SUBNET MASK", s.mask},
            {"FIRST USABLE IP", s.first,     "LAST USABLE IP", s.last,          "BROADCAST",   s.broadcast}
        };
        Color[][] colors2 = {
            {null, ACCENT, null, TEXT, null, TEXT},
            {null, GREEN,  null, GREEN, null, AMBER}
        };

        JPanel grid = new JPanel(new GridLayout(2, 3, 0, 0));
        grid.setOpaque(false);

        int cellIdx = 0;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                JPanel cell = new DarkPanel(new Color(0,0,0,0)) {
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.setColor(BORDER);
                        g.drawLine(0, 0, getWidth(), 0);
                        g.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
                    }
                };
                cell.setOpaque(false);
                cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
                cell.setBorder(new EmptyBorder(10,16,10,16));

                JLabel lbl = new JLabel(cells[row][col*2]);
                lbl.setFont(MONO_SM.deriveFont(9f));
                lbl.setForeground(TEXT3);
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

                Color vc = colors2[row][col*2+1];
                JLabel val = new JLabel(cells[row][col*2+1]);
                val.setFont(MONO);
                val.setForeground(vc != null ? vc : TEXT);
                val.setAlignmentX(Component.LEFT_ALIGNMENT);

                cell.add(lbl); cell.add(vStrut(3)); cell.add(val);
                grid.add(cell);
                cellIdx++;
            }
        }

        card.add(top);
        card.add(grid);
        return card;
    }

    private JPanel buildSummaryPanel(long networkAddr, long totalAddrs, int prefix, String addrStr) {
        long used = results.stream().mapToLong(r -> r.size).sum();
        int util = (int)Math.round(used * 100.0 / totalAddrs);

        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG1);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(BORDER);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18,20,18,20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel utilRow = new DarkPanel(new Color(0,0,0,0));
        utilRow.setOpaque(false);
        utilRow.setLayout(new BorderLayout());
        JLabel uLbl = new JLabel("Address utilization");
        uLbl.setFont(MONO_SM); uLbl.setForeground(TEXT2);
        JLabel uPct = new JLabel(util + "%");
        uPct.setFont(MONO_SM); uPct.setForeground(ACCENT);
        utilRow.add(uLbl, BorderLayout.WEST);
        utilRow.add(uPct, BorderLayout.EAST);
        utilRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        utilRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        card.add(utilRow);
        card.add(vStrut(8));

        card.add(new UtilBar(util));
        card.add(vStrut(16));

        // stat grid
        Object[][] stats = {
            {"PARENT NETWORK",   longToIp(networkAddr)+"/"+prefix},
            {"TOTAL ADDRESSES",  String.format("%,d", totalAddrs)},
            {"SUBNETS CREATED",  String.valueOf(results.size())},
            {"ADDRESSES USED",   String.format("%,d", used)},
            {"ADDRESSES FREE",   String.format("%,d", totalAddrs - used)},
        };
        JPanel grid = new JPanel(new GridLayout(1, stats.length, 10, 0));
        grid.setOpaque(false);
        for (Object[] stat : stats) {
            JPanel cell = new DarkPanel(BG2);
            cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
            cell.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(8,12,8,12)
            ));
            JLabel l = new JLabel((String)stat[0]);
            l.setFont(MONO_SM.deriveFont(9f)); l.setForeground(TEXT3);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel v = new JLabel((String)stat[1]);
            v.setFont(MONO); v.setForeground(TEXT);
            v.setAlignmentX(Component.LEFT_ALIGNMENT);
            cell.add(l); cell.add(vStrut(3)); cell.add(v);
            grid.add(cell);
        }
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(grid);
        return card;
    }

    // ── Export ────────────────────────────────────────────────────
    private void exportCSV() {
        if (results.isEmpty()) return;
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("vlsm-subnets.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter pw = new PrintWriter(fc.getSelectedFile())) {
            pw.println("Name,Network,CIDR,Mask,First IP,Last IP,Broadcast,Requested Hosts,Usable Hosts");
            for (SubnetResult r : results)
                pw.printf("%s,%s,/%d,%s,%s,%s,%s,%d,%d%n",
                    r.name,r.network,r.prefix,r.mask,r.first,r.last,r.broadcast,r.requested,r.usable);
            JOptionPane.showMessageDialog(this, "Exported to " + fc.getSelectedFile().getName(), "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            showError("Export failed: " + ex.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────
    private void showError(String msg) {
        lblError.setText(msg);
        javax.swing.Timer t = new javax.swing.Timer(4000, e -> lblError.setText(" "));
        t.setRepeats(false); t.start();
    }

    static long ipToLong(String ip) {
        String[] p = ip.split("\\.");
        if (p.length != 4) return -1;
        try {
            long r = 0;
            for (String s : p) { int v = Integer.parseInt(s); if (v<0||v>255) return -1; r = r*256+v; }
            return r;
        } catch (NumberFormatException e) { return -1; }
    }
    static String longToIp(long n) {
        return ((n>>24)&255)+"."+((n>>16)&255)+"."+((n>>8)&255)+"."+(n&255);
    }
    static long cidrToMask(int bits) {
        return bits == 0 ? 0 : (0xFFFFFFFFL << (32-bits)) & 0xFFFFFFFFL;
    }
    static int bitsNeeded(int h) {
        int b = 1; while ((1<<b)-2 < h) b++; return b;
    }

    // ── UI helpers ────────────────────────────────────────────────
    static JPanel buildCard(String title, JComponent content) {
        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG1);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(BORDER);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel header = new DarkPanel(new Color(0,0,0,0)) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BORDER);
                g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        header.setOpaque(false);
        header.setLayout(new FlowLayout(FlowLayout.LEFT));
        header.setBorder(new EmptyBorder(2,6,2,6));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = new JLabel(title);
        t.setFont(MONO_SM.deriveFont(10f));
        t.setForeground(TEXT2);
        header.add(t);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        card.add(header);

        if (content != null) {
            content.setBorder(new EmptyBorder(14,18,14,18));
            content.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(content);
        }
        return card;
    }

    static JTextField darkField(String val) {
        JTextField f = new JTextField(val) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG2);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                if (isFocusOwner()) {
                    g2.setColor(new Color(0,200,255,40));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.setColor(new Color(0,200,255,100));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                } else {
                    g2.setColor(BORDER);
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setFont(MONO);
        f.setForeground(TEXT);
        f.setCaretColor(ACCENT);
        f.setBackground(new Color(0,0,0,0));
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(8,12,8,12));
        f.setPreferredSize(new Dimension(100, 36));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return f;
    }

    static JButton ghostButton(String text) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) g2.setColor(BG3);
                else g2.setColor(new Color(0,0,0,0));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(getModel().isRollover() ? BORDER2 : BORDER);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(MONO_SM);
        b.setForeground(TEXT2);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    static JButton accentButton(String text) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(ACCENT2);
                else if (getModel().isRollover()) g2.setColor(new Color(0x33,0xd4,0xff));
                else g2.setColor(ACCENT);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(MONO_SM.deriveFont(Font.BOLD));
        b.setForeground(Color.BLACK);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    static JButton smallButton(String text) {
        JButton b = ghostButton(text);
        b.setFont(MONO_SM.deriveFont(10f));
        b.setForeground(TEXT3);
        b.setBorder(new EmptyBorder(3,8,3,8));
        return b;
    }

    static JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setFont(MONO_SM);
        b.setForeground(TEXT3);
        b.setBackground(null);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(ACCENT); }
            public void mouseExited(MouseEvent e)  { b.setForeground(TEXT3); }
        });
        return b;
    }

    static JLabel label10(String text) {
        JLabel l = new JLabel(text);
        l.setFont(MONO_SM.deriveFont(10f));
        l.setForeground(TEXT3);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    static Component vStrut(int h) {
        Component c = Box.createVerticalStrut(h);
        ((JComponent)c).setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    static Component vGlue() {
        Component c = Box.createVerticalGlue();
        ((JComponent)c).setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    static void styleScrollbar(JScrollPane sp) {
        JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            protected void configureScrollBarColors() { this.thumbColor = BG3; this.trackColor = BG; }
            protected JButton createDecreaseButton(int o) { return zeroButton(); }
            protected JButton createIncreaseButton(int o) { return zeroButton(); }
            JButton zeroButton() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
        });
        vsb.setPreferredSize(new Dimension(4,0));
    }

    // ── Inner types ───────────────────────────────────────────────
    static class DarkPanel extends JPanel {
        DarkPanel(Color bg) { super(); setBackground(bg); setOpaque(bg.getAlpha() > 0); }
    }

    static class SubnetReq {
        String name; String hostsStr; int hosts;
        JTextField nameField, hostField;
        SubnetReq(String n, String h) { name=n; hostsStr=h; }
    }

    static class SubnetResult {
        String name, mask, network, first, last, broadcast;
        int requested, usable, prefix; long size, netLong;
    }

    static class RoundLabel extends JLabel {
        Color bg, border;
        RoundLabel(String text, Font f, Color fg, Color bg, Color border) {
            super(text); setFont(f); setForeground(fg);
            this.bg=bg; this.border=border;
            setBorder(new EmptyBorder(4,12,4,12));
            setOpaque(false);
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
            g2.setColor(border); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
            g2.dispose(); super.paintComponent(g);
        }
    }

    static class AddressMapPanel extends JPanel {
        List<SubnetResult> subnets; long networkAddr, totalAddrs;
        AddressMapPanel(List<SubnetResult> s, long na, long ta) {
            subnets=s; networkAddr=na; totalAddrs=ta;
            setOpaque(false);
            setPreferredSize(new Dimension(100, 32));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            // background
            g2.setColor(BG2);
            g2.fillRoundRect(0,0,w,h,8,8);

            long prev = networkAddr;
            double scale = (double)w / totalAddrs;
            for (int i=0; i<subnets.size(); i++) {
                SubnetResult s = subnets.get(i);
                Color c = SUBNET_COLORS[i % SUBNET_COLORS.length];
                if (s.netLong > prev) {
                    int gx = (int)((prev-networkAddr)*scale);
                    int gw = (int)((s.netLong-prev)*scale);
                    // hatching for gap
                    g2.setColor(new Color(255,255,255,8));
                    g2.fillRect(gx,0,gw,h);
                }
                int sx = (int)((s.netLong-networkAddr)*scale);
                int sw = Math.max(2,(int)(s.size*scale));
                g2.setColor(c);
                g2.fillRect(sx,0,sw,h);
                prev = s.netLong + s.size;
            }
            // border
            g2.setColor(BORDER);
            g2.drawRoundRect(0,0,w-1,h-1,8,8);
            g2.dispose();
        }
    }

    static class UtilBar extends JPanel {
        int pct;
        UtilBar(int pct) {
            this.pct=pct;
            setOpaque(false);
            setPreferredSize(new Dimension(100,6));
            setMaximumSize(new Dimension(Integer.MAX_VALUE,6));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BG3); g2.fillRoundRect(0,0,getWidth(),6,6,6);
            int fw=getWidth()*pct/100;
            GradientPaint gp=new GradientPaint(0,0,ACCENT2,fw,0,ACCENT);
            g2.setPaint(gp);
            g2.fillRoundRect(0,0,fw,6,6,6);
            g2.dispose();
        }
    }

    // WrapLayout so legend wraps nicely
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align,hgap,vgap); }
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap*2);
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;
                int nmembers = target.getComponentCount();
                for (int i=0; i<nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            dim.width = Math.max(dim.width, rowWidth);
                            dim.height += rowHeight + vgap;
                            rowWidth = 0; rowHeight = 0;
                        }
                        rowWidth += d.width + hgap;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                dim.width = Math.max(dim.width, rowWidth);
                dim.height += rowHeight + insets.top + insets.bottom + vgap*2;
                return dim;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        UIManager.put("Panel.background",    BG);
        UIManager.put("TextField.background",BG2);
        UIManager.put("TextField.foreground",TEXT);
        UIManager.put("TextField.caretForeground",ACCENT);
        UIManager.put("ScrollPane.background",BG);
        UIManager.put("Viewport.background", BG);
        SwingUtilities.invokeLater(VLSMCalculator::new);
    }
}

