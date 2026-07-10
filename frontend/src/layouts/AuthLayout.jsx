import { Outlet } from "react-router-dom";
import logo from "../assets/logo_xochi.png";
import {
    Box,
    Grid,
    Typography
} from "@mui/material";

export default function AuthLayout() {

    return (

        <Grid
            container
            sx={{
                minHeight: "100vh"
            }}
        >

            {/* Panel izquierdo */}

            <Grid
                size={{ xs: 0, md: 6 }}
                sx={{
                    display: {
                        xs: "none",
                        md: "flex"
                    },
                    backgroundImage: "url('/images/login-bg.jpg')",
                    backgroundSize: "cover",
                    backgroundPosition: "center",
                    position: "relative"
                }}
            >

                <Box
                    sx={{
                        position: "absolute",
                        inset: 0,
                        bgcolor: "white"
                    }}
                />

                <Box
                    sx={{
                        position: "relative",
                        zIndex: 2,
                        color: "black",
                        p: 8,
                        display: "flex",
                        flexDirection: "column",
                        justifyContent: "center"
                    }}
                >
                    <Box
                        component="img"
                        src={logo}
                        alt="Logo Ayuntamiento de Xochitepec"
                        sx={{
                            width: 350,
                            height: "auto",
                            mb: 4,
                        }}
                    />

                    <Typography
                        variant="h3"
                        fontWeight="bold"
                    >
                        Sistema de Gestión
                    </Typography>

                    <Typography
                        variant="h5"
                        mt={2}
                    >
                        Patrimonio Municipal
                    </Typography>

                    <Typography
                        mt={4}
                    >
                        Administración y control de los bienes
                        municipales del Ayuntamiento.
                    </Typography>

                </Box>

            </Grid>

            {/* Panel derecho */}

            <Grid
                size={{ xs: 12, md: 6 }}
            >

                <Box
                    sx={{
                        height: "100%",
                        display: "flex",
                        justifyContent: "center",
                        alignItems: "center",
                        p: 4
                    }}
                >

                    <Outlet />

                </Box>

            </Grid>

        </Grid>

    );

}